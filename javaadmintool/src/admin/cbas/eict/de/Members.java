package admin.cbas.eict.de;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.security.cert.X509CRLEntry;
import java.util.LinkedList;

import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AbstractDocument;

import admin.cbas.eict.de.MemberAuthorityAPI.Member;
import admin.cbas.eict.de.SliceAuthorityAPI.AnObject;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Members extends JPanel{
	
	private static final long serialVersionUID = -2394649760751128917L;
	JList userList;
	JButton buttonEditUser, buttonAddUser, buttonRevokeUser, buttonExtendMembership;
	SortedListModel<MemberAuthorityAPI.Member> listModelMembers;
	SortedListModel<String> listModelProjects, listModelSlices;
	private JPanel panelRight;
	private JSplitPane splitPaneProjectSlices;
	private JScrollPane scrollPaneProjects;
	private JList listProjects, listSlices;
	private JScrollPane scrollPaneSlices;
	private JLabel lblFirstName;
	private JTextField tfFirstName;
	private JLabel lblLastName;
	private JTextField tfLastName;
	private JLabel lblEmail;
	private JTextField tfEmail;
	private JLabel lblUrn;
	private JTextField tfUserURN;
	private JLabel lblCertificateCreation;
	private JTextField tfValidFrom;
	private JLabel lblCertificateExpiration;
	private JTextField tfValidUntil;
	private JLabel lblMembershipStatus;
	private JTextField tfMembershipStatus;
	JFileChooser fileChooser;
	static Color DARK_GREEN = new Color(30,200,60);
	final MainGUI mainGUI;

	/**
	 * Create the application.
	 */
	public Members(MainGUI mainWindow, Member[] memberData) {
		
		mainGUI = mainWindow;
			
		setLayout(new BorderLayout());
		
		JScrollPane scrollPane = new JScrollPane();
		listModelMembers = new SortedListModel<Member>();
		if(memberData != null)
			listModelMembers.addAll(memberData);
		userList = new JList(listModelMembers);
		userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(userList);
		TitledBorder titled = new TitledBorder("List of Members");
		scrollPane.setBorder(titled);
		scrollPane.setPreferredSize(new Dimension(300,400));
		buttonEditUser = new JButton("Edit Info");
		buttonEditUser.addActionListener( new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if(userList.isSelectionEmpty())
				{
					JOptionPane.showMessageDialog(null, "Select a user from list to edit.");
					return;
				}
				
				Member toEdit = (Member) userList.getSelectedValue();
				Member changes = getMemberDetailInput(toEdit);
				if( changes != null)
				{
					changes.urn = toEdit.urn;
					boolean rsp = MemberAuthorityAPI.updateMemberInfo(changes);
					if(rsp == true)
					{						
						toEdit.fName = changes.fName;
						toEdit.lName = changes.lName;
						toEdit.email = changes.email;
		                tfFirstName.setText(changes.fName);
		                tfLastName.setText(changes.lName);
		                tfEmail.setText(changes.email);
					}
					else
						showErrorMessage();
				}
			}			
		});
		buttonAddUser = new JButton("New Member");
		buttonAddUser.addActionListener( new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
								
				Member d = getMemberDetailInput(null);
				if( d != null)
				{
					Member rsp = MemberAuthorityAPI.addMember(d);
					if(rsp != null)
					{
						listModelMembers.add(rsp);
						saveCertificateAndKey(rsp, "Member has been added.");
						userList.setSelectedValue(rsp, true);
					}
					else
						showErrorMessage();
					
				}
			}			
		});
		
		buttonRevokeUser = new JButton("Revoke Certificate");
		buttonRevokeUser.addActionListener( new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if(userList.isSelectionEmpty())
				{
					JOptionPane.showMessageDialog(null, "First select a user from list.");
					return;
				}
				

				Member toRevoke = (Member) userList.getSelectedValue();
                X509CRLEntry e = MemberAuthorityAPI.crl.getRevokedCertificate(toRevoke.cert);
                
                if(e!=null)
                {
					JOptionPane.showMessageDialog(null, "The certificate is already in revoked state.");
					return;                	
                }
                
				
				if(toRevoke.username.equals("root") || toRevoke.username.equals("expedient"))
				{
					JOptionPane.showMessageDialog(null, toRevoke.username+" is a privileged user. This membership cannot be revoked.");
					return;                						
				}
				
				
				boolean rsp = MemberAuthorityAPI.reovkeMembership(toRevoke);
				if(rsp == true)
				{
	                  e = MemberAuthorityAPI.crl.getRevokedCertificate(toRevoke.cert);
	                  tfMembershipStatus.setText(e==null?"Active":"Revoked");
	                  tfMembershipStatus.setForeground(e==null?DARK_GREEN:Color.RED);
				}
				else
					showErrorMessage();
									
			}			
		});		
		buttonExtendMembership = new JButton("Renew Certificate");
		buttonExtendMembership.addActionListener( new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {

				if(userList.isSelectionEmpty())
				{
					JOptionPane.showMessageDialog(null, "First select a user from list.");
					return;
				}
				

				Member toRenew = (Member) userList.getSelectedValue();
				
				if(toRenew.username.equals("root") || toRenew.username.equals("expedient"))
				{
					JOptionPane.showMessageDialog(null, toRenew.username+" is a privileged user. This membership cannot be modified.");
					return;                						
				}
				
				Member rsp = MemberAuthorityAPI.extendMembership(toRenew);
				if(rsp != null)
				{
					toRenew.cert = rsp.cert;
					toRenew.certStr = rsp.certStr;
					toRenew.privateKey = rsp.privateKey;
					saveCertificateAndKey(toRenew, "Membership has been extended.");
	                tfValidFrom.setText(Utils.formatDate(toRenew.cert.getNotBefore()));
	                tfValidUntil.setText(Utils.formatDate(toRenew.cert.getNotAfter()));
	                tfMembershipStatus.setText("Active");	
	                tfMembershipStatus.setForeground(DARK_GREEN);
				}
				else
					showErrorMessage();
									
			}			
		});
		
		panelRight = new JPanel();		
		panelRight.setLayout(new BorderLayout(0, 0));
		
		JPanel infoPanel = new JPanel();
		panelRight.add(infoPanel, BorderLayout.NORTH);
		infoPanel.setBorder(new TitledBorder("Member Information"));
		GridBagLayout gbl_infoPanel = new GridBagLayout();
		gbl_infoPanel.columnWeights = new double[]{0.0, 1.0};
		infoPanel.setLayout(gbl_infoPanel);
		
		lblFirstName = new JLabel("First Name:");
		GridBagConstraints gbc_lblFirstName = new GridBagConstraints();
		gbc_lblFirstName.insets = new Insets(0, 0, 7, 5);
		gbc_lblFirstName.anchor = GridBagConstraints.EAST;
		gbc_lblFirstName.gridx = 0;
		gbc_lblFirstName.gridy = 0;
		infoPanel.add(lblFirstName, gbc_lblFirstName);
		
		tfFirstName = new JTextField();
		tfFirstName.setEditable(false);
		GridBagConstraints gbc_tfFirstName = new GridBagConstraints();
		gbc_tfFirstName.insets = new Insets(0, 0, 7, 0);
		gbc_tfFirstName.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfFirstName.gridx = 1;
		gbc_tfFirstName.gridy = 0;
		infoPanel.add(tfFirstName, gbc_tfFirstName);
		tfFirstName.setColumns(10);
		
		tfLastName = new JTextField();
		tfLastName.setEditable(false);
		GridBagConstraints gbc_tfLastName = new GridBagConstraints();
		gbc_tfLastName.insets = new Insets(0, 0, 7, 0);
		gbc_tfLastName.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfLastName.gridx = 1;
		gbc_tfLastName.gridy = 1;
		infoPanel.add(tfLastName, gbc_tfLastName);
		tfLastName.setColumns(10);
		
		lblLastName = new JLabel("Last Name:");
		GridBagConstraints gbc_lblLastName = new GridBagConstraints();
		gbc_lblLastName.anchor = GridBagConstraints.EAST;
		gbc_lblLastName.insets = new Insets(0, 0, 7, 5);
		gbc_lblLastName.gridx = 0;
		gbc_lblLastName.gridy = 1;
		infoPanel.add(lblLastName, gbc_lblLastName);
		
		lblEmail = new JLabel("Email:");
		GridBagConstraints gbc_lblEmail = new GridBagConstraints();
		gbc_lblEmail.anchor = GridBagConstraints.EAST;
		gbc_lblEmail.insets = new Insets(0, 0, 7, 5);
		gbc_lblEmail.gridx = 0;
		gbc_lblEmail.gridy = 2;
		infoPanel.add(lblEmail, gbc_lblEmail);
		
		tfEmail = new JTextField();
		tfEmail.setEditable(false);
		GridBagConstraints gbc_tfEmail = new GridBagConstraints();
		gbc_tfEmail.insets = new Insets(0, 0, 7, 0);
		gbc_tfEmail.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfEmail.gridx = 1;
		gbc_tfEmail.gridy = 2;
		infoPanel.add(tfEmail, gbc_tfEmail);
		tfEmail.setColumns(10);
		
		lblUrn = new JLabel("URN:");
		GridBagConstraints gbc_lblUrn = new GridBagConstraints();
		gbc_lblUrn.anchor = GridBagConstraints.EAST;
		gbc_lblUrn.insets = new Insets(0, 0, 7, 5);
		gbc_lblUrn.gridx = 0;
		gbc_lblUrn.gridy = 3;
		infoPanel.add(lblUrn, gbc_lblUrn);
		
		tfUserURN = new JTextField();
		tfUserURN.setEditable(false);
		GridBagConstraints gbc_tfUserURN = new GridBagConstraints();
		gbc_tfUserURN.insets = new Insets(0, 0, 7, 0);
		gbc_tfUserURN.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfUserURN.gridx = 1;
		gbc_tfUserURN.gridy = 3;
		infoPanel.add(tfUserURN, gbc_tfUserURN);
		tfUserURN.setColumns(10);
		
		lblCertificateCreation = new JLabel("Certificate Creation:");
		GridBagConstraints gbc_lblCertificateCreation = new GridBagConstraints();
		gbc_lblCertificateCreation.anchor = GridBagConstraints.EAST;
		gbc_lblCertificateCreation.insets = new Insets(0, 0, 7, 5);
		gbc_lblCertificateCreation.gridx = 0;
		gbc_lblCertificateCreation.gridy = 4;
		infoPanel.add(lblCertificateCreation, gbc_lblCertificateCreation);
		
		tfValidFrom = new JTextField();
		tfValidFrom.setEditable(false);
		GridBagConstraints gbc_tfValidFrom = new GridBagConstraints();
		gbc_tfValidFrom.insets = new Insets(0, 0, 7, 0);
		gbc_tfValidFrom.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfValidFrom.gridx = 1;
		gbc_tfValidFrom.gridy = 4;
		infoPanel.add(tfValidFrom, gbc_tfValidFrom);
		tfValidFrom.setColumns(10);
		
		lblCertificateExpiration = new JLabel("Certificate Expiration:");
		GridBagConstraints gbc_lblCertificateExpiration = new GridBagConstraints();
		gbc_lblCertificateExpiration.anchor = GridBagConstraints.EAST;
		gbc_lblCertificateExpiration.insets = new Insets(0, 0, 7, 5);
		gbc_lblCertificateExpiration.gridx = 0;
		gbc_lblCertificateExpiration.gridy = 5;
		infoPanel.add(lblCertificateExpiration, gbc_lblCertificateExpiration);
		
		tfValidUntil = new JTextField();
		tfValidUntil.setEditable(false);
		GridBagConstraints gbc_tfValidUntil = new GridBagConstraints();
		gbc_tfValidUntil.insets = new Insets(0, 0, 7, 0);
		gbc_tfValidUntil.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfValidUntil.gridx = 1;
		gbc_tfValidUntil.gridy = 5;
		infoPanel.add(tfValidUntil, gbc_tfValidUntil);
		tfValidUntil.setColumns(10);
		
		lblMembershipStatus = new JLabel("Membership Status:");
		GridBagConstraints gbc_lblMembershipStatus = new GridBagConstraints();
		gbc_lblMembershipStatus.anchor = GridBagConstraints.EAST;
		gbc_lblMembershipStatus.insets = new Insets(0, 0, 10, 5);
		gbc_lblMembershipStatus.gridx = 0;
		gbc_lblMembershipStatus.gridy = 6;
		infoPanel.add(lblMembershipStatus, gbc_lblMembershipStatus);
		
		tfMembershipStatus = new JTextField();
		tfMembershipStatus.setEditable(false);
		GridBagConstraints gbc_tfMembershipStatus = new GridBagConstraints();
		gbc_tfMembershipStatus.insets = new Insets(0, 0, 10, 0);
		gbc_tfMembershipStatus.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfMembershipStatus.gridx = 1;
		gbc_tfMembershipStatus.gridy = 6;
		infoPanel.add(tfMembershipStatus, gbc_tfMembershipStatus);
		tfMembershipStatus.setColumns(10);
		
		JSplitPane mid = new JSplitPane();		
		mid.setLeftComponent(scrollPane);
		mid.setRightComponent(panelRight);
		add(mid, BorderLayout.CENTER);
		
		splitPaneProjectSlices = new JSplitPane();
		panelRight.add(splitPaneProjectSlices, BorderLayout.CENTER);		
		
		listModelSlices = new SortedListModel<String>();
		listSlices= new JList(listModelSlices);	
		listSlices.setToolTipText("Double click an entry to see its details");
		listSlices.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2)
				{
					mainGUI.setSelectedSlice((String)((JList)e.getSource()).getSelectedValue());
				}
			}
		});
		scrollPaneSlices = new JScrollPane(listSlices);
		scrollPaneSlices.setBorder(new TitledBorder("Member Slices"));
		splitPaneProjectSlices.setRightComponent(scrollPaneSlices);
		
		listModelProjects = new SortedListModel<String>();
		listProjects = new JList(listModelProjects);
		listProjects.setToolTipText("Double click an entry to see its details");
		listProjects.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2)
				{
					mainGUI.setSelectedProject((String)((JList)e.getSource()).getSelectedValue());
				}
			}
		});
		scrollPaneProjects = new JScrollPane(listProjects);
		scrollPaneProjects.setBorder(new TitledBorder("Member Projects"));
		splitPaneProjectSlices.setLeftComponent(scrollPaneProjects);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(buttonRevokeUser);
		buttonPanel.add(buttonExtendMembership);
		buttonPanel.add(buttonEditUser);
		buttonPanel.add(buttonAddUser);
		add(buttonPanel, BorderLayout.SOUTH);
		
		userList.addListSelectionListener( new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                if (!arg0.getValueIsAdjusting()) {
  				  Member mem = (Member) userList.getSelectedValue();
                  
                  tfFirstName.setText(mem.fName);
                  tfLastName.setText(mem.lName);
                  tfEmail.setText(mem.email);
                  //tfUsername.setText(memberDetails.get(index).username);
                  tfUserURN.setText(mem.urn);
                  //tfUserUUID.setText(memberDetails.get(index).uuid);                  
                  tfValidFrom.setText(Utils.formatDate(mem.cert.getNotBefore()));
                  tfValidUntil.setText(Utils.formatDate(mem.cert.getNotAfter()));
                  X509CRLEntry e = MemberAuthorityAPI.crl.getRevokedCertificate(mem.cert);
                  tfMembershipStatus.setText(e==null?"Active":"Revoked");
                  tfMembershipStatus.setForeground(e==null?DARK_GREEN:Color.RED);
                  
                  LinkedList<AnObject> slices = SliceAuthorityAPI.lookupForMembers(mem.urn, "SLICE"); 
                  if(slices != null)
                  {
	                  listModelSlices.clear();
	                  for(int i=0; i<slices.size(); i++)
	                	  listModelSlices.add(slices.get(i).name);
                  }
                  else
                	  JOptionPane.showMessageDialog(null, "Could not fetch member slices.", "Error", JOptionPane.ERROR_MESSAGE);;
                  
                  LinkedList<AnObject> projects = SliceAuthorityAPI.lookupForMembers(mem.urn, "PROJECT");                  
                  if(projects != null)
                  {
                	  listModelProjects.clear();
                	  for(int i=0; i<projects.size(); i++)
                		  listModelProjects.add(projects.get(i).name);
                  }
                  else
                	  JOptionPane.showMessageDialog(null, "Could not fetch member projects.", "Error", JOptionPane.ERROR_MESSAGE);;

                }
            }
        }); 
		
		if(listModelMembers.getSize()>0)
			userList.setSelectedIndex(0);
		
		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	
	}
	
	private void showErrorMessage()
	{
		JOptionPane.showMessageDialog(
			    this, 
			    "<html><body><p style='width: 300px;'>"+MemberAuthorityAPI.output+"</p></body></html>", 
			    "Error", 
			    JOptionPane.ERROR_MESSAGE);
	}
	
	private Member getMemberDetailInput(Member memDetails)
	{
		 
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10,0,0,0);  //top padding
		c.weightx = 0.1;
		c.gridx = 0;
		c.gridy = 0;				
		infoPanel.add(new JLabel("First Name:"),c);
		c.gridy = 1;						
		infoPanel.add(new JLabel("Last Name:"),c);
		c.gridy = 2;						
		infoPanel.add(new JLabel("Email:"),c);
		
		JTextField tfFirstName, tfLastName, tfEmail, tfUsername;
		tfFirstName = new JTextField(memDetails==null?"":memDetails.fName);
		tfLastName = new JTextField(memDetails==null?"":memDetails.lName);
		tfEmail = new JTextField(memDetails==null?"":memDetails.email);	

		tfUsername = new JTextField(memDetails==null?"":memDetails.username);
	    ((AbstractDocument) tfUsername.getDocument()).setDocumentFilter(new PatternFilter("^[a-zA-Z][\\w]{0,7}"));
		
		c.weightx = 0.9;
		c.gridx = 1;
		c.gridy = 0;		
		infoPanel.add(tfFirstName, c);
		c.gridy = 1;		
		infoPanel.add(tfLastName, c);
		c.gridy = 2;		
		infoPanel.add(tfEmail, c);
		
		if( memDetails==null ) 
		{
			c.gridy = 3;		
			infoPanel.add(tfUsername, c);
			c.weightx = 0.1;
			c.gridx = 0;						
			infoPanel.add(new JLabel("Username:"),c);
		}
		
		int response; 
		do{
			response = JOptionPane.showConfirmDialog(null, infoPanel,memDetails==null?"Add New Member":"Edit Member Details ",
	                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			tfUsername.setBackground(tfUsername.getText().trim().length()==0?Color.pink:Color.white);
			tfFirstName.setBackground(tfFirstName.getText().trim().length()==0?Color.pink:Color.white);
			tfLastName.setBackground(tfLastName.getText().trim().length()==0?Color.pink:Color.white);
			tfEmail.setBackground(tfEmail.getText().trim().length()==0?Color.pink:Color.white);
		}
		while( (tfUsername.getText().length()==0  ||
			    tfFirstName.getText().length()==0 ||
			    tfLastName.getText().length()==0 ||
			    tfEmail.getText().length()==0) &&	
			   response == JOptionPane.OK_OPTION);
		
		if(response != JOptionPane.OK_OPTION)
			return null;
		else
		{
			Member d = new Member();
			d.fName = tfFirstName.getText().trim();
			d.lName = tfLastName.getText().trim();
			d.email = tfEmail.getText().trim();
			d.username = tfUsername.getText().trim();
			
			return d;
		}
	}
	
	private void saveCertificateAndKey(final Member md, String message)
	{
		Object[] options = {"Save"};
		final JOptionPane optionPane = new JOptionPane(message+"\nPlease save member certificate and key.", 
									 JOptionPane.INFORMATION_MESSAGE, JOptionPane.YES_OPTION, null, options, options[0]);
		
		final JDialog dialog = new JDialog();
		dialog.setContentPane(optionPane);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		optionPane.addPropertyChangeListener(
			    new PropertyChangeListener() {
			        public void propertyChange(PropertyChangeEvent e) {
			            String prop = e.getPropertyName();

			            if (dialog.isVisible() 
			             && (e.getSource() == optionPane)
			             && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
			                dialog.dispose();

			                int result = fileChooser.showSaveDialog(null);
			        		if (result == JFileChooser.APPROVE_OPTION) {
			        		    File dir = fileChooser.getSelectedFile();
			        		    try {
			        				PrintWriter out = new PrintWriter(new File(dir, md.username+"-cert.pem"));
			        				out.print(md.certStr);
			        				out.close();
			        				out = new PrintWriter(new File(dir, md.username+"-key.pem"));
			        				out.print(md.privateKey);			
			        				out.close();
			        			} catch (FileNotFoundException e1) {
			        				// TODO Auto-generated catch block
			        				e1.printStackTrace();
			        			}
			        		}
			            }
			        }
			    });		
		dialog.pack();
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}
	
	
	public Object[] getMemberArray()
	{
		return listModelMembers.toArray();
	}
	
	
}

package org.waarp.xample.custnodes;

/*
 * Copyright (c) 2003 Felix Golubov
 */

import java.util.*;
import java.util.List;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.fg.ftreenodes.Params;
import com.fg.ftreenodes.ICellControl;

/**
 * FFileDialog is a file dialog, which can be used as a custom field
 * editor for the {@link com.fg.xmleditor.FXBasicView}. Since FFileDialog is
 * a JDialog, it can be used as both custom field editor and a global editor.
 *
 * @author Felix Golubov
 * @author frederic Bregier
 * @version 2.0
 * 
 * Add support for Directory and different options on File Dialog
 */

 /*
   Implementation notices:
   Well, it would much better to make buttons of the JFileChooser invisible
   and create our own "OK" and "Cancel" buttons. The problem is that
   CDE/Motif UI doesn't allow to make buttons invisible. Hence all the
   complexities with addBtnListener(Component c) recursive method. Besides,
   when UI changes, all the child components of the JFileChooser are discarded,
   so addBtnListener has to be called from the updateUI() method.
 */

public class FFileDialog extends JDialog
  implements ICellControl, ActionListener
{
  JPanel panel = new JPanel();
  FFileChooser fileChooser = new FFileChooser();
  int mode = JFileChooser.FILES_ONLY;
  Object data;
  List masks = null;

  class FileExtFilter extends javax.swing.filechooser.FileFilter
  {
    HashSet<String> extensions = new HashSet<String>();
    String description = "";

    public FileExtFilter(String mask)
    {
      StringTokenizer st = new StringTokenizer(mask, "|");
      while (st.hasMoreElements())
      {
        String token = st.nextToken().trim();
        if (st.hasMoreElements()) {
            if (token.length()==0) {
                // no extension
            } else {
                if (token.startsWith("null")) {
                    if (token.length()>4) {
                        token = token.substring(5).trim();
                    } else {
                        token = "";
                    }
                }
                if (token.length()>0) {
                    extensions.add(token.toLowerCase());
                }
            }
        } else {
            String type = token.substring(0, 1);
            int val = Integer.parseInt(type);
            if (val == 0) {
                mode = JFileChooser.FILES_ONLY;
            } else if (val == 1) {
                mode = JFileChooser.DIRECTORIES_ONLY;
            } else {
                mode = JFileChooser.FILES_AND_DIRECTORIES;
            }
            description = token.substring(1);
        }
      }
      fileChooser.setFileSelectionMode(mode);
    }

    public boolean accept(File f)
    {
      if (f.isDirectory())
        return true;
      if (f.isFile() && mode == JFileChooser.DIRECTORIES_ONLY)
          return false;
      if (extensions.size() == 0)
          return true;
      String ext = null;
      String name = f.getName();
      int i = name.lastIndexOf('.');
      if (i > 0 &&  i < name.length() - 1)
      {
        ext = name.substring(i + 1).toLowerCase();
        return extensions.contains(ext);
      }
      else
        return false;
    }

    public String getDescription()
    {
      return description;
    }
  }

  class FFileChooser extends JFileChooser
  {
    public void addBtnListener(Component c)
    {
      if (c instanceof JButton)
        ((JButton)c).addActionListener(FFileDialog.this);
      if (c instanceof Container)
      {
        Component[] children = ((Container)c).getComponents();
        for (int i = 0; i < children.length; i++)
          addBtnListener(children[i]);
      }
    }

    public void updateUI()
    {
      setAcceptAllFileFilterUsed(getAcceptAllFileFilter() == null);
      super.updateUI();
      setAcceptAllFileFilterUsed(true);
      addBtnListener(this);
    }
  }

  public FFileDialog()
  {
    this.getContentPane().setLayout(new BorderLayout());
    fileChooser.setCurrentDirectory(new File("."));
    fileChooser.addActionListener(this);
    fileChooser.setApproveButtonText("OK");
    this.getContentPane().add(fileChooser, BorderLayout.CENTER);
  }

  protected void processWindowEvent(WindowEvent e)
  {
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      cancel();
    }
    super.processWindowEvent(e);
  }

  void cancel()
  {
    dispose();
  }

  public void actionPerformed(ActionEvent e)
  {
    if (!(e.getSource() instanceof JButton))
      return;
    JButton btn = (JButton)e.getSource();
    if ("OK".equals(btn.getText()))
    {
      File file = fileChooser.getSelectedFile();
      //data = (file == null || file.isDirectory()) ? "" : file.getAbsolutePath();
      data = (file == null) ? "" : file.getAbsolutePath();
      cancel();
    }
    else if ("Cancel".equals(btn.getText()))
      cancel();
    else if ("Annuler".equals(btn.getText()))
        cancel();
  }

  public void initCellControl(boolean isEditor)
  {
    fileChooser.addBtnListener(fileChooser);
  }

  public void updateCellControl(boolean isEditor, boolean enabled,
    boolean editable, Object data, Params params)
  {
    pack();
    this.data = data;
    if (data != null)
    {
      String path = data.toString().trim();
      if (path.length() > 0)
      {
        try {
          File file = new File(path);
          fileChooser.setSelectedFile(file);
        } catch (Exception ex) {
          fileChooser.setSelectedFile(null);
        }
      }
      else
        fileChooser.setSelectedFile(null);
    }
    else
      fileChooser.setSelectedFile(null);
    List list = params.getList();
    boolean sameMasks = true;
    if (masks == null || list.size() != masks.size())
      sameMasks = false;
    else
    {
      for (int i = 0; i < list.size() && sameMasks; i++)
      {
        if (!list.get(i).equals(masks.get(i)))
          sameMasks = false;
      }
    }
    if (sameMasks)
      return;
    masks = list;
    fileChooser.resetChoosableFileFilters();
    for (int i = 0; i < masks.size(); i++)
    {
      fileChooser.addChoosableFileFilter(
        new FileExtFilter((String)masks.get(i)));
    }
  }

  public Object getData()
  {
    return data;
  }

}

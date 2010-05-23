/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Caps.java
 *
 * Created on 01-feb-2010, 21:21:28
 */

package es.atareao.caps.gui;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import es.atareao.alejandria.gui.AboutDialogo;
import es.atareao.alejandria.gui.ErrorDialog;
import es.atareao.alejandria.gui.ExtensionFilter;
import es.atareao.alejandria.lib.AppUtil;
import es.atareao.alejandria.lib.Convert;
import es.atareao.alejandria.lib.FileUtils;
import es.atareao.alejandria.lib.GeneradorUUID;
import es.atareao.alejandria.lib.INIFile;
import es.atareao.alejandria.lib.Preferencias;
import es.atareao.alejandria.lib.StringUtils;
import java.awt.Cursor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author atareao
 */
public class Caps extends javax.swing.JFrame {
    //
    //********************************CONSTANTES********************************
    //
    public static final long serialVersionUID=0L;
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;
    //
    public final static int OP_ADD=0;
    public final static int OP_EDIT=1;
    public final static int OP_DELETE=2;
    public final static int OP_VIEW=3;
    public final static int OP_PRINTPREVIEW=4;
    public final static int OP_PRINT=5;
    public final static int OP_PARTE=6;
    public final static int OP_DUPLICATE=7;
    //
    //
    private final static int AC_NUEVO=0;
    private final static int AC_ABRIR=1;
    private final static int AC_CERRAR=2;
    private final static int AC_GUARDAR=3;
    private final static int AC_GUARDAR_COMO=4;
    private final static int AC_CALCULA=5;
    private final static int AC_PRINTPREVIEW=6;
    //
    private final static String VERSION="01.00.20100201";
    private final static String APPNAME="Caps";
    private final static String EXT=".cap";
    /** Creates new form Caps */
    public Caps(INIFile ini) {
        initComponents();
        this.setSize(500,830);
        this.setLocationRelativeTo(null);
        this.setPreferencias(new Preferencias(ini));
        AppUtil.verificaVersion(this,APPNAME,VERSION);
        this.doWhenOpenFile(false);

    }
    private void doWhenOpenFile(boolean isFileOpened){
        if(!isFileOpened){
            this.setTitle(APPNAME);
        }
        this.setOpened(isFileOpened);
        this.jTipo.setEnabled(_opened);
        this.jMaterial.setEnabled(isFileOpened);
        this.jDiametro.setEnabled(isFileOpened);
        this.jPresion.setEnabled(isFileOpened);
        //
        this.jButton3.setEnabled(isFileOpened);
        this.jButton4.setEnabled(isFileOpened);
        this.jButton5.setEnabled(isFileOpened);
        this.jMenuArchivoCerrar.setEnabled(isFileOpened);
        this.jMenuArchivoGuardar.setEnabled(isFileOpened);
        this.jMenuArchivoGuardarComo.setEnabled(isFileOpened);
    }
    private boolean closeFile(){
        this.setOpened(false);
        if(this.isModificado()){
            if(JOptionPane.YES_OPTION==JOptionPane.showConfirmDialog(this,"El archivo ha sido modificado, ¿Quiere guardarlo?", "Atención",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)){
                return this.saveData();
            }
        }
        return true;
    }
    private boolean loadData(){
        this.jTipo.setSelectedItem(this.getDatos().getStringProperty("Data","Tipo"));
        this.jMaterial.setSelectedItem(this.getDatos().getStringProperty("Data","Material"));
        this.jDiametro.setDouble(this.getDatos().getDoubleProperty("Data","Diametro"));
        this.jPresion.setDouble(this.getDatos().getDoubleProperty("Data","Presion"));
        return true;
    }
    private boolean saveData(){
        this.getDatos().setStringProperty("Data","Tipo",(String)this.jTipo.getSelectedItem(),null);
        this.getDatos().setStringProperty("Data","Material",(String)this.jMaterial.getSelectedItem(),null);
        this.getDatos().setDoubleProperty("Data","Diametro",this.jDiametro.getDouble(),null);
        this.getDatos().setDoubleProperty("Data","Presion",this.jPresion.getDouble(),null);
        return this.getDatos().save();
    }
    private boolean isModificado(){
        if(this.isOpened()){
            boolean mod=true;
            if(!this.getDatos().getStringProperty("Data","Tipo").equals(this.jTipo.getSelectedItem())){
                return false;
            }
            if(!this.getDatos().getStringProperty("Data","Material").equals(this.jMaterial.getSelectedItem())){
                return false;
            }
            if(this.getDatos().getDoubleProperty("Data","Diametro")!=this.jDiametro.getDouble()){
                return false;
            }
            if(this.getDatos().getDoubleProperty("Data","Presion")!=this.jPresion.getDouble()){
                return false;
            }
            return mod;
        }
        return false;
    }
    private boolean createData(){
        this.getDatos().addSection("Data",null);
        this.getDatos().setStringProperty("Data","Tipo","Kloepper",null);
        this.getDatos().setStringProperty("Data","Material","S235JO",null);
        this.getDatos().setDoubleProperty("Data","Diametro",0.0,null);
        this.getDatos().setDoubleProperty("Data","Presion",0.0,null);
        return true;
    }
    private boolean openFile(File file){
        if((file!=null)&&(file.isFile())&&(file.exists())){
            this.setDatos(new INIFile(file.getAbsolutePath()));
            this.setTitle(APPNAME + " - " + file.getAbsolutePath());
            if(this.loadData()){
                this.setOpened(true);
                this.doWhenOpenFile(true);
                this.setDefaultDir(this.getDefaultDir());
                this.addRecentFile(file.getAbsolutePath());
                return true;
            }
        }else{
            JOptionPane.showMessageDialog(this,"El archivo \""+file.getName()+"\" no existe",APPNAME,JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    private void doAction(int action){
        boolean doit=false;
        switch (action){
            case AC_ABRIR:
                this.closeFile();
                ExtensionFilter ef = new ExtensionFilter("*"+EXT,EXT);
                JFileChooser jfc = new JFileChooser(this.getDefaultDir());
                jfc.setAcceptAllFileFilterUsed(false);
                jfc.setFileFilter(ef);
                if (JFileChooser.APPROVE_OPTION == jfc.showOpenDialog(this)) {
                        File file = jfc.getSelectedFile();
                        this.setDefaultDir(file.getParentFile());
                        this.openFile(file);
                        this.doWhenOpenFile(true);
                }
                break;
            case AC_CERRAR:
                doit=this.closeFile();
                if(doit){
                    this.doWhenOpenFile(false);
                }
                break;
            case AC_GUARDAR:
                if(this.isOpened()){
                   this.saveData();
                }else{
                    ef = new ExtensionFilter("*"+EXT,EXT);
                    jfc = new JFileChooser(this.getDefaultDir());
                    jfc.setAcceptAllFileFilterUsed(false);
                    jfc.setFileFilter(ef);
                    if (JFileChooser.APPROVE_OPTION == jfc.showSaveDialog(this)) {
                            File file = jfc.getSelectedFile();
                            if(!file.getAbsolutePath().endsWith(EXT)){
                                file=new File(file.getAbsolutePath()+EXT);
                            }
                            this.setDefaultDir(file.getParentFile());
                            this.setTitle(APPNAME+" - " + file.getAbsolutePath());
                            this.setDatos(new INIFile(file.getAbsolutePath()));
                            this.setDefaultDir(this.getDefaultDir());
                            this.addRecentFile(file.getAbsolutePath());
                            this.setDatos(new INIFile(file));
                            this.createData();
                            this.saveData();
                    }
                    break;

                }
                break;
            case AC_GUARDAR_COMO:
                ef = new ExtensionFilter("*"+EXT,EXT);
                jfc = new JFileChooser(this.getDefaultDir());
                jfc.setAcceptAllFileFilterUsed(false);
                jfc.setFileFilter(ef);
                if (JFileChooser.APPROVE_OPTION == jfc.showSaveDialog(this)) {
                        File file = jfc.getSelectedFile();
                        if(!file.getAbsolutePath().endsWith(EXT)){
                            file=new File(file.getAbsolutePath()+EXT);
                        }
                        this.setDefaultDir(file.getParentFile());
                        this.setTitle(APPNAME+" - " + file.getAbsolutePath());
                        this.setDatos(new INIFile(file.getAbsolutePath()));
                        this.setDefaultDir(this.getDefaultDir());
                        this.addRecentFile(file.getAbsolutePath());
                        this.setDatos(new INIFile(file));
                        this.createData();
                        this.saveData();
                }
                break;
            case AC_NUEVO:
                    ef = new ExtensionFilter("*"+EXT,EXT);
                    jfc = new JFileChooser(this.getDefaultDir());
                    jfc.setAcceptAllFileFilterUsed(false);
                    jfc.setFileFilter(ef);
                    if (JFileChooser.APPROVE_OPTION == jfc.showSaveDialog(this)) {
                            File file = jfc.getSelectedFile();
                            if(!file.getAbsolutePath().endsWith(EXT)){
                                file=new File(file.getAbsolutePath()+EXT);
                            }
                            this.setDefaultDir(file.getParentFile());
                            this.setTitle(APPNAME+" - " + file.getAbsolutePath());
                            this.setDatos(new INIFile(file.getAbsolutePath()));
                            this.setDefaultDir(this.getDefaultDir());
                            this.addRecentFile(file.getAbsolutePath());
                            this.setDatos(new INIFile(file));
                            this.createData();
                            this.saveData();
                            this.doWhenOpenFile(true);
                    }
                break;
            case AC_CALCULA:
                String tipo=(String)this.jTipo.getSelectedItem();
                String material=(String)this.jMaterial.getSelectedItem();
                double De=this.jDiametro.getDouble();
                double P=this.jPresion.getDouble();
                //
                this.calcula_espesor(tipo, material, De, P);
                break;
            case AC_PRINTPREVIEW:
                this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                try {
                    tipo=(String)this.jTipo.getSelectedItem();
                    material=(String)this.jMaterial.getSelectedItem();
                    De=this.jDiametro.getDouble();
                    P=this.jPresion.getDouble();
                    //
                    PdfReader p = new PdfReader(FileUtils.getInputStreamFromJar("/es/atareao/caps/tpl/resultados.pdf"));
                    String filename="temporal_caps_"+GeneradorUUID.crearUUID()+"_file.pdf";
                    File file=new File(filename);
                    if(file.exists()){
                        while(!file.delete()){}
                    }
                    PdfStamper stamp = new PdfStamper(p, new FileOutputStream(filename));
                    AcroFields form = stamp.getAcroFields();
                    for(int contador=0;contador<=22;contador++){
                        String valor="valor"+StringUtils.rellena(Convert.toString(contador),"00");
                        form.setField(valor,this._valores.get(contador));
                    }
                    stamp.setFormFlattening(true);
                    stamp.close();
                    while(!file.exists()){}
                    PrintPreviewDialog ppd=new PrintPreviewDialog(this,file,this.getDefaultDir());
                    ppd.setVisible(true);
                    file.delete();
                } catch (DocumentException ex) {
                    ErrorDialog.manejaError(ex);
                } catch (IOException ex) {
                    ErrorDialog.manejaError(ex);
                }
                this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                break;
        }
    }

    private void addRecentFile(String file){
        String file0=this.getPreferencias().getPreferencia("File0");
        String file1=this.getPreferencias().getPreferencia("File1");
        String file2=this.getPreferencias().getPreferencia("File2");
        String file3=this.getPreferencias().getPreferencia("File3");
        String file4=this.getPreferencias().getPreferencia("File4");
        if(file!=null){
            if(file.equals(file0)){
                return;
            }
            if(file.equals(file1)){
                file1=file0;
                file0=file;
            }else if(file.equals(file2)){
                file2=file0;
                file0=file;
            }else if(file.equals(file3)){
                file3=file0;
                file0=file;
            }else if(file.equals(file4)){
                file4=file0;
                file0=file;
            }else{
                file4=file3;
                file3=file2;
                file2=file1;
                file1=file0;
                file0=file;
            }
        }else{
            file0=file1;
            file1=file2;
            file2=file3;
            file3=file4;
            file4=null;

        }
        if(file0!=null){
            this.jMenuArchivoFile0.setText(file0);
            this.getPreferencias().setPreferencia("File0", file0);
        }else{
            this.jMenuArchivoFile0.setText("");
        }
        if(file1!=null){
            this.jMenuArchivoFile1.setText(file1);
            this.getPreferencias().setPreferencia("File1", file1);
        }else{
            this.jMenuArchivoFile1.setText("");
        }
        if(file2!=null){
            this.jMenuArchivoFile2.setText(file2);
            this.getPreferencias().setPreferencia("File2", file2);
        }else{
            this.jMenuArchivoFile2.setText("");
        }
        if(file3!=null){
            this.jMenuArchivoFile3.setText(file3);
            this.getPreferencias().setPreferencia("File3", file3);
        }else{
            this.jMenuArchivoFile3.setText("");
        }
        if(file4!=null){
            this.jMenuArchivoFile4.setText(file4);
            this.getPreferencias().setPreferencia("File4", file4);
        }else{
            this.jMenuArchivoFile4.setText("");
        }
        this.getPreferencias().save();
    }
    private void doExit() {
        this.getPreferencias().save();
        this.closeFile();
        System.exit(0);
    }
    private double normaliza_espesor(double espesor){
        double[] espesores={2,2.5,3,4,5,6,8,10,12,14,15,16,18,20,22,25,30,35,40,45,50,55,60,65,70,75,80,90,100,110,120,130,140,150};
        for(double valor:espesores){
            if(espesor<=valor){
                return valor;
            }
        }
        return espesor;
    }
    private String format(String format, double number,String unidades){
        //return formatter.format(number)
        NumberFormat formatter = new DecimalFormat(format);
        if((unidades!=null)&&(unidades.length()>0)){
            return formatter.format(number)+" "+unidades;
        }
        return formatter.format(number);
    }
    private void calcula_espesor(String tipo,String material, double De,double P){
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        this._valores=new ArrayList<String>();
        this._valores.add(tipo);//1
        this._valores.add(material);//2
        this._valores.add(format("#,##0.0",De,"mm"));//3
        this._valores.add(format("#,##0.000",P,"MPa"));//4
        this._valores.add(format("#,##0.000",P,"MPa"));//5
        double R=0;
        double r=0;
        double z=1;
        double re;
        double rm;
        double f;
        double es;
        double et=0;
        double Di;
        double beta;
        double ey;
        double eb;
        double fb;
        double e;
        double en;
        double hi;
        double ea;
        if(tipo.equals("Kloepper")){
            R=De;
            r=0.1*De;
        }else{
            R=0.8*De;
            r=0.154*De;
        }
        this._valores.add(format("#,##0.0",R,"mm"));//6
        this._valores.add(format("#,##0.0",r,"mm"));//7
        do{
            es=et;
            re=calcula_re(material,es);
            rm=calcula_rm(material,es);
            f=Math.min((re/1.5),rm/2.4);
            et=(P*R)/(2*f*z-0.5*P);
        }while(Math.abs(es-et)>0.001);
        do{
            ey=et;
            Di=De-2*ey;
            beta=calcula_beta(De,ey,R,r);
            re=calcula_re(material,ey);
            rm=calcula_rm(material,ey);
            f=Math.min((re/1.5),rm/2.4);
            et=beta*P*(0.75*R+0.2*Di)/f;
        }while(Math.abs(ey-et)>0.001);
        //
        do{
            eb=et;
            Di=De-2*eb;
            re=calcula_re(material,eb);
            rm=calcula_rm(material,eb);
            fb=re/1.5;
            et=(0.75*R+0.2*Di)*Math.pow(P/(111*fb)*Math.pow(Di/r,0.825),(1/1.5));
        }while(Math.abs(eb-et)>0.001);
        //
        double[] espesores={es,eb,ey};
        e=calcula_maximo(espesores);
        en=normaliza_espesor(e);
        Di=De-2*en;
        hi=R-Math.sqrt((R-Di/2)*(R+Di/2-2*r));
        re=calcula_re(material,en);
        rm=calcula_rm(material,en);
        this._valores.add(format("#,##0.0",re,"MPa"));//8
        this._valores.add(format("#,##0.0",rm,"MPa"));//9
        this._valores.add("1");//10
        f=Math.min((re/1.5),rm/2.4);
        this._valores.add(format("#,##0.0",f,"MPa"));//11
        this._valores.add(format("#,##0.0",es,"mm"));//12
        this._valores.add(format("#,##0.000",beta,null));//13
        this._valores.add(format("#,##0.0",eb,"mm"));//14
        this._valores.add(format("#,##0.0",fb,"MPa"));//15
        this._valores.add(format("#,##0.0",ey,"mm"));//16
        this._valores.add(format("#,##0.0",en,"mm"));//17
        this._valores.add(format("#,##0.0",Di,"mm"));//18
        this._valores.add(format("#,##0.0",hi,"mm"));//19
        //
        //
        //
        this.jR.setDouble(R);
        this.jr.setDouble(r);
        this.je.setDouble(en);
        this.jDi.setDouble(Di);
        this.jh.setDouble(hi);
        //
        //
        //
        ea=en;
        re=calcula_re(material,ea);
        rm=calcula_rm(material,ea);
        f=Math.min((re/1.5),rm/2.4);
        fb=re/1.5;
        double ps=2*f*z*ea/(R+0.5*ea);
        double py=f*ea/(calcula_beta(De,ea,R,r)*(0.75*R+0.2*(De-2*ea)));
        double pb=111*fb*Math.pow(ea/(0.75*R+0.2*De),1.5)*Math.pow(r/(De-2*ea),0.825);
        double[] maxp={ps,py,pb};
        double pmax=calcula_minimo(maxp);
        this.jPmax.setDouble(pmax);
        this._valores.add(format("#,##0.0",ps,"MPa"));//20
        this._valores.add(format("#,##0.0",py,"MPa"));//21
        this._valores.add(format("#,##0.0",pb,"MPa"));//22
        this._valores.add(format("#,##0.0",pmax,"MPa"));//23
        //
        //
        //
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    private double calcula_minimo(double[] valores){
        double minimo=Double.POSITIVE_INFINITY;
        for(double valor:valores){
            minimo=Math.min(minimo,valor);
        }
        return minimo;
    }
    private double calcula_maximo(double[] valores){
        double maximo=Double.NEGATIVE_INFINITY;
        for(double valor:valores){
            maximo=Math.max(maximo,valor);
        }
        return maximo;
    }
    private double calcula_beta(double De,double e, double R, double r){
    	double Di=De-2*e;
        double Y=0;
        double Z;
        double X;
        double N;
        double Beta006;
        double Beta01;
        double Beta02;
	if(e/R>0.04){
            Y=0.04;
        }else{
            Y=e/R;
        }
	Z=Math.log10(1/Y);
	X=r/Di;
	N=1.006-1/(6.2+Math.pow(90*Y,4.0));
	Beta006=N*(-0.3635*Math.pow(Z,3.0)+2.2124*Math.pow(Z,2.0)-3.2937*Z+1.8873);
	Beta01=N*(-0.1833*Math.pow(Z,3.0)+1.0383*Math.pow(Z,2.0)-1.2943*Z+0.837);
	Beta02=0.95*(0.56-1.94*Y-82.5*Math.pow(Y,2.0));
        if(Beta02<0.5){
            Beta02=0.5;
        }
	if(X<0.06){
            return Beta006;
        }
        if((X>0.06)&&(X<0.1)){
            return 25*((0.1-X)*Beta006+(X-0.06)*Beta01);
        }
	if(X==0.1){
            return Beta01;
        }
	if((X>0.1)&&(X<0.2)){
            return 10*((0.2-X)*Beta01+(X-0.1)*Beta02);
        }
	if(X==0.2){
            return Beta02;
        }
        return -1;
    }
    private double calcula_rm(String material, double espesor){
        double[] espesores={3,100,150,250,400};
        double[] S235JR={360,360,350,340};
        double[] S235J0={360,360,350,340};
        double[] S235J2={360,360,350,340,330};
        double[] S275JR={430,410,400,380};
        double[] S275J0={430,410,400,380};
        double[] S275J2={430,410,400,380,380};
        double[] S355JR={510,470,450,450};
        double[] S355J0={510,470,450,450};
        double[] S355J2={510,470,450,450,450};
        double[] S355K2={510,470,450,450,450};
        double[] S450J0={550,550,530};
        int seleccionado=-1;
        do{
            seleccionado++;
        }while(espesor>espesores[seleccionado]);
        if(seleccionado==-1){
            return -1;
        }
        if(material.equals("S235JR")){
            if(S235JR.length<seleccionado+1){
                return -1;
            }
            return S235JR[seleccionado];
        }
        if(material.equals("S235J0")){
            if(S235J0.length<seleccionado+1){
                return -1;
            }
            return S235J0[seleccionado];
        }
        if(material.equals("S235J2")){
            if(S235J2.length<seleccionado+1){
                return -1;
            }
            return S235J2[seleccionado];
        }
        //
        if(material.equals("S275JR")){
            if(S275JR.length<seleccionado+1){
                return -1;
            }
            return S275JR[seleccionado];
        }
        if(material.equals("S275J0")){
            if(S275J0.length<seleccionado+1){
                return -1;
            }
            return S275J0[seleccionado];
        }
        if(material.equals("S275J2")){
            if(S275J2.length<seleccionado+1){
                return -1;
            }
            return S275J2[seleccionado];
        }
        //
        if(material.equals("S355JR")){
            if(S355JR.length<seleccionado+1){
                return -1;
            }
            return S355JR[seleccionado];
        }
        if(material.equals("S355J0")){
            if(S355J0.length<seleccionado+1){
                return -1;
            }
            return S355J0[seleccionado];
        }
        if(material.equals("S355J2")){
            if(S355J2.length<seleccionado+1){
                return -1;
            }
            return S355J2[seleccionado];
        }
        if(material.equals("S355K2")){
            if(S355K2.length<seleccionado+1){
                return -1;
            }
            return S355K2[seleccionado];
        }
        //
        if(material.equals("S450J0")){
            if(S450J0.length<seleccionado+1){
                return -1;
            }
            return S450J0[seleccionado];
        }
        return -1;
    }
    private double calcula_re(String material,double espesor){
        double[] espesores={16,40,63,80,100,150,200,250,400};
        double[] S235JR={235,225,215,215,215,195,185,175};
        double[] S235J0={235,225,215,215,215,195,185,175};
        double[] S235J2={235,225,215,215,215,195,185,175,165};
        double[] S275JR={275,265,255,245,235,225,215,205};
        double[] S275J0={275,265,255,245,235,225,215,205};
        double[] S275J2={275,265,255,245,235,225,215,205,195};
        double[] S355JR={355,345,335,325,315,295,285,275};
        double[] S355J0={355,345,335,325,315,295,285,275};
        double[] S355J2={355,345,335,325,315,295,285,275,265};
        double[] S355K2={355,345,335,325,315,295,285,275,265};
        double[] S450J0={450,430,410,390,380,380};
        int seleccionado=-1;
        do{
            seleccionado++;
        }while(espesor>espesores[seleccionado]);
        if(seleccionado==-1){
            return -1;
        }
        if(material.equals("S235JR")){
            if(S235JR.length<seleccionado+1){
                return -1;
            }
            return S235JR[seleccionado];
        }
        if(material.equals("S235J0")){
            if(S235J0.length<seleccionado+1){
                return -1;
            }
            return S235J0[seleccionado];
        }
        if(material.equals("S235J2")){
            if(S235J2.length<seleccionado+1){
                return -1;
            }
            return S235J2[seleccionado];
        }
        //
        if(material.equals("S275JR")){
            if(S275JR.length<seleccionado+1){
                return -1;
            }
            return S275JR[seleccionado];
        }
        if(material.equals("S275J0")){
            if(S275J0.length<seleccionado+1){
                return -1;
            }
            return S275J0[seleccionado];
        }
        if(material.equals("S275J2")){
            if(S275J2.length<seleccionado+1){
                return -1;
            }
            return S275J2[seleccionado];
        }
        //
        if(material.equals("S355JR")){
            if(S355JR.length<seleccionado+1){
                return -1;
            }
            return S355JR[seleccionado];
        }
        if(material.equals("S355J0")){
            if(S355J0.length<seleccionado+1){
                return -1;
            }
            return S355J0[seleccionado];
        }
        if(material.equals("S355J2")){
            if(S355J2.length<seleccionado+1){
                return -1;
            }
            return S355J2[seleccionado];
        }
        if(material.equals("S355K2")){
            if(S355K2.length<seleccionado+1){
                return -1;
            }
            return S355K2[seleccionado];
        }
        //
        if(material.equals("S450J0")){
            if(S450J0.length<seleccionado+1){
                return -1;
            }
            return S450J0[seleccionado];
        }
        return -1;
    }
    private double calcula_C(double Re,double D,double Q, double K,double beta){
        double A=Math.pow((190000*beta/Re),0.8);
        double C=
        0.5961+0.0261*Math.pow(beta,2.0)-0.216*Math.pow(beta,8.0)+
        0.000521*Math.pow((beta*1000000/Re),0.7)+
        (0.0188+0.0063*A)*Math.pow(beta,3.5)*Math.pow((1000000/Re),0.3)+
        (0.043+0.08-0.123)*(1-0.11*A)*Math.pow(beta,4)/(1-Math.pow(beta,4));
        return C;
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButton5 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jImage1 = new es.atareao.alejandria.gui.JImage();
        jImage2 = new es.atareao.alejandria.gui.JImage();
        jImage3 = new es.atareao.alejandria.gui.JImage();
        jImage4 = new es.atareao.alejandria.gui.JImage();
        jTipo = new javax.swing.JComboBox();
        jMaterial = new javax.swing.JComboBox();
        jDiametro = new es.atareao.alejandria.gui.JNumericField();
        jPresion = new es.atareao.alejandria.gui.JNumericField();
        jPanel2 = new javax.swing.JPanel();
        jImage5 = new es.atareao.alejandria.gui.JImage();
        jImage6 = new es.atareao.alejandria.gui.JImage();
        jImage7 = new es.atareao.alejandria.gui.JImage();
        jImage8 = new es.atareao.alejandria.gui.JImage();
        jImage9 = new es.atareao.alejandria.gui.JImage();
        jImage10 = new es.atareao.alejandria.gui.JImage();
        jImage11 = new es.atareao.alejandria.gui.JImage();
        jR = new es.atareao.alejandria.gui.JNumericField();
        jr = new es.atareao.alejandria.gui.JNumericField();
        je = new es.atareao.alejandria.gui.JNumericField();
        jDi = new es.atareao.alejandria.gui.JNumericField();
        jh = new es.atareao.alejandria.gui.JNumericField();
        jPmax = new es.atareao.alejandria.gui.JNumericField();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuArchivo = new javax.swing.JMenu();
        jMenuArchivoNuevo = new javax.swing.JMenuItem();
        jMenuArchivoAbrir = new javax.swing.JMenuItem();
        jMenuArchivoCerrar = new javax.swing.JMenuItem();
        jMenuArchivoGuardar = new javax.swing.JMenuItem();
        jMenuArchivoGuardarComo = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuArchivoFiles = new javax.swing.JMenu();
        jMenuArchivoFile0 = new javax.swing.JMenuItem();
        jMenuArchivoFile1 = new javax.swing.JMenuItem();
        jMenuArchivoFile2 = new javax.swing.JMenuItem();
        jMenuArchivoFile3 = new javax.swing.JMenuItem();
        jMenuArchivoFile4 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jMenuItemSalir = new javax.swing.JMenuItem();
        jMenuAyuda = new javax.swing.JMenu();
        jMenuItemAcercaDe = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Caps");
        setIconImage(new javax.swing.ImageIcon(getClass().getResource("/es/atareao/caps/img/fondo_icono.png")).getImage());
        setResizable(false);

        jToolBar1.setRollover(true);
        jToolBar1.setMaximumSize(new java.awt.Dimension(32767, 50));
        jToolBar1.setMinimumSize(new java.awt.Dimension(16, 50));
        jToolBar1.setPreferredSize(new java.awt.Dimension(100, 50));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/es/atareao/img/png/button_add.png"))); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setMaximumSize(new java.awt.Dimension(50, 50));
        jButton1.setMinimumSize(new java.awt.Dimension(50, 50));
        jButton1.setPreferredSize(new java.awt.Dimension(50, 50));
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/es/atareao/img/png/button_open.png"))); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setMaximumSize(new java.awt.Dimension(50, 50));
        jButton2.setMinimumSize(new java.awt.Dimension(50, 50));
        jButton2.setPreferredSize(new java.awt.Dimension(50, 50));
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/es/atareao/img/png/button_save.png"))); // NOI18N
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setMaximumSize(new java.awt.Dimension(50, 50));
        jButton3.setMinimumSize(new java.awt.Dimension(50, 50));
        jButton3.setPreferredSize(new java.awt.Dimension(50, 50));
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton3);

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/es/atareao/img/png/button_calculate.png"))); // NOI18N
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setMaximumSize(new java.awt.Dimension(50, 50));
        jButton4.setMinimumSize(new java.awt.Dimension(50, 50));
        jButton4.setPreferredSize(new java.awt.Dimension(50, 50));
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton4);
        jToolBar1.add(jSeparator1);

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/es/atareao/img/png/button_print_preview.png"))); // NOI18N
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setMaximumSize(new java.awt.Dimension(50, 50));
        jButton5.setMinimumSize(new java.awt.Dimension(50, 50));
        jButton5.setPreferredSize(new java.awt.Dimension(50, 50));
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton5);

        getContentPane().add(jToolBar1, java.awt.BorderLayout.PAGE_START);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos"));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jImage1.setHorizontalImageAlignment(es.atareao.alejandria.gui.JImage.HorizontalAlignment.LEFT);
        jImage1.setResourceUrl("/es/atareao/caps/img/img01.png");
        jPanel3.add(jImage1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 240, 30));

        jImage2.setHorizontalImageAlignment(es.atareao.alejandria.gui.JImage.HorizontalAlignment.LEFT);
        jImage2.setResourceUrl("/es/atareao/caps/img/img02.png");
        jPanel3.add(jImage2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 240, 30));

        jImage3.setHorizontalImageAlignment(es.atareao.alejandria.gui.JImage.HorizontalAlignment.LEFT);
        jImage3.setResourceUrl("/es/atareao/caps/img/img03.png");
        jPanel3.add(jImage3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, 240, 30));

        jImage4.setHorizontalImageAlignment(es.atareao.alejandria.gui.JImage.HorizontalAlignment.LEFT);
        jImage4.setResourceUrl("/es/atareao/caps/img/img04.png");
        jPanel3.add(jImage4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 140, 240, 30));

        jTipo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Kloepper", "Korbbogen" }));
        jPanel3.add(jTipo, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 20, 200, 30));

        jMaterial.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "S235JR", "S235J0", "S235J2", "S275JR", "S275J0", "S275J2", "S355JR", "S355J0", "S355J2", "S355K2", "S450J0" }));
        jPanel3.add(jMaterial, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 60, 200, 30));

        jDiametro.setDecimals(1);
        jPanel3.add(jDiametro, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 100, 200, 30));

        jPresion.setDecimals(3);
        jPanel3.add(jPresion, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 140, 200, 30));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 470, 190));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Resultados"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jImage5.setHorizontalImageAlignment(es.atareao.alejandria.gui.JImage.HorizontalAlignment.LEFT);
        jImage5.setResourceUrl("/es/atareao/caps/img/img21.png");
        jPanel2.add(jImage5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 470, 240, 30));

        jImage6.setResourceUrl("/es/atareao/caps/img/geometria.png");
        jImage6.setScaleHeight(0.32);
        jImage6.setScaleWidth(0.32);
        jPanel2.add(jImage6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, -10, 450, 290));

        jImage7.setHorizontalImageAlignment(es.atareao.alejandria.gui.JImage.HorizontalAlignment.LEFT);
        jImage7.setResourceUrl("/es/atareao/caps/img/img06.png");
        jPanel2.add(jImage7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 310, 240, 30));

        jImage8.setHorizontalImageAlignment(es.atareao.alejandria.gui.JImage.HorizontalAlignment.LEFT);
        jImage8.setResourceUrl("/es/atareao/caps/img/img18.png");
        jPanel2.add(jImage8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 350, 240, 30));

        jImage9.setHorizontalImageAlignment(es.atareao.alejandria.gui.JImage.HorizontalAlignment.LEFT);
        jImage9.setResourceUrl("/es/atareao/caps/img/img19.png");
        jPanel2.add(jImage9, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 390, 240, 30));

        jImage10.setHorizontalImageAlignment(es.atareao.alejandria.gui.JImage.HorizontalAlignment.LEFT);
        jImage10.setResourceUrl("/es/atareao/caps/img/img20.png");
        jPanel2.add(jImage10, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 430, 240, 30));

        jImage11.setHorizontalImageAlignment(es.atareao.alejandria.gui.JImage.HorizontalAlignment.LEFT);
        jImage11.setResourceUrl("/es/atareao/caps/img/img05.png");
        jPanel2.add(jImage11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 270, 240, 30));

        jR.setEditable(false);
        jR.setDecimals(3);
        jPanel2.add(jR, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 270, 200, 30));

        jr.setEditable(false);
        jr.setDecimals(3);
        jPanel2.add(jr, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 310, 200, 30));

        je.setEditable(false);
        je.setDecimals(3);
        jPanel2.add(je, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 350, 200, 30));

        jDi.setEditable(false);
        jDi.setDecimals(3);
        jPanel2.add(jDi, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 390, 200, 30));

        jh.setEditable(false);
        jh.setDecimals(3);
        jPanel2.add(jh, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 430, 200, 30));

        jPmax.setEditable(false);
        jPmax.setDecimals(3);
        jPmax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPmaxActionPerformed(evt);
            }
        });
        jPanel2.add(jPmax, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 470, 200, 30));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, 470, 510));

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jMenuArchivo.setText("Archivo");

        jMenuArchivoNuevo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        jMenuArchivoNuevo.setText("Nuevo");
        jMenuArchivoNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuArchivoNuevoActionPerformed(evt);
            }
        });
        jMenuArchivo.add(jMenuArchivoNuevo);

        jMenuArchivoAbrir.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        jMenuArchivoAbrir.setText("Abrir");
        jMenuArchivoAbrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuArchivoAbrirActionPerformed(evt);
            }
        });
        jMenuArchivo.add(jMenuArchivoAbrir);

        jMenuArchivoCerrar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        jMenuArchivoCerrar.setText("Cerrar");
        jMenuArchivoCerrar.setEnabled(false);
        jMenuArchivoCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuArchivoCerrarActionPerformed(evt);
            }
        });
        jMenuArchivo.add(jMenuArchivoCerrar);

        jMenuArchivoGuardar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
        jMenuArchivoGuardar.setText("Guardar");
        jMenuArchivoGuardar.setEnabled(false);
        jMenuArchivoGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuArchivoGuardarActionPerformed(evt);
            }
        });
        jMenuArchivo.add(jMenuArchivoGuardar);

        jMenuArchivoGuardarComo.setText("Guardar como ...");
        jMenuArchivoGuardarComo.setEnabled(false);
        jMenuArchivoGuardarComo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuArchivoGuardarComoActionPerformed(evt);
            }
        });
        jMenuArchivo.add(jMenuArchivoGuardarComo);
        jMenuArchivo.add(jSeparator2);

        jMenuArchivoFiles.setText("Recientes");

        jMenuArchivoFile0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuArchivoFile0ActionPerformed(evt);
            }
        });
        jMenuArchivoFiles.add(jMenuArchivoFile0);

        jMenuArchivoFile1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuArchivoFile1ActionPerformed(evt);
            }
        });
        jMenuArchivoFiles.add(jMenuArchivoFile1);

        jMenuArchivoFile2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuArchivoFile2ActionPerformed(evt);
            }
        });
        jMenuArchivoFiles.add(jMenuArchivoFile2);

        jMenuArchivoFile3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuArchivoFile3ActionPerformed(evt);
            }
        });
        jMenuArchivoFiles.add(jMenuArchivoFile3);

        jMenuArchivoFile4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuArchivoFile4ActionPerformed(evt);
            }
        });
        jMenuArchivoFiles.add(jMenuArchivoFile4);

        jMenuArchivo.add(jMenuArchivoFiles);
        jMenuArchivo.add(jSeparator3);

        jMenuItemSalir.setText("Salir");
        jMenuItemSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSalirActionPerformed(evt);
            }
        });
        jMenuArchivo.add(jMenuItemSalir);

        jMenuBar1.add(jMenuArchivo);

        jMenuAyuda.setText("Ayuda");

        jMenuItemAcercaDe.setText("A cerca de...");
        jMenuItemAcercaDe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAcercaDeActionPerformed(evt);
            }
        });
        jMenuAyuda.add(jMenuItemAcercaDe);

        jMenuBar1.add(jMenuAyuda);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuArchivoNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuArchivoNuevoActionPerformed
        this.doAction(AC_NUEVO);
}//GEN-LAST:event_jMenuArchivoNuevoActionPerformed

    private void jMenuArchivoAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuArchivoAbrirActionPerformed
        this.doAction(AC_ABRIR);
}//GEN-LAST:event_jMenuArchivoAbrirActionPerformed

    private void jMenuArchivoCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuArchivoCerrarActionPerformed
        this.doAction(AC_CERRAR);
}//GEN-LAST:event_jMenuArchivoCerrarActionPerformed

    private void jMenuArchivoGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuArchivoGuardarActionPerformed
        this.doAction(AC_GUARDAR);
}//GEN-LAST:event_jMenuArchivoGuardarActionPerformed

    private void jMenuArchivoGuardarComoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuArchivoGuardarComoActionPerformed
        this.doAction(AC_GUARDAR_COMO);
}//GEN-LAST:event_jMenuArchivoGuardarComoActionPerformed

    private void jMenuArchivoFile0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuArchivoFile0ActionPerformed
        File file=new File(this.jMenuArchivoFile0.getText());
        this.openFile(file);
}//GEN-LAST:event_jMenuArchivoFile0ActionPerformed

    private void jMenuArchivoFile1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuArchivoFile1ActionPerformed
        File file=new File(this.jMenuArchivoFile1.getText());
        this.openFile(file);
}//GEN-LAST:event_jMenuArchivoFile1ActionPerformed

    private void jMenuArchivoFile2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuArchivoFile2ActionPerformed
        File file=new File(this.jMenuArchivoFile2.getText());
        this.openFile(file);
}//GEN-LAST:event_jMenuArchivoFile2ActionPerformed

    private void jMenuArchivoFile3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuArchivoFile3ActionPerformed
        File file=new File(this.jMenuArchivoFile3.getText());
        this.openFile(file);
}//GEN-LAST:event_jMenuArchivoFile3ActionPerformed

    private void jMenuArchivoFile4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuArchivoFile4ActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_jMenuArchivoFile4ActionPerformed

    private void jMenuItemSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSalirActionPerformed
        this.doExit();
}//GEN-LAST:event_jMenuItemSalirActionPerformed

    private void jMenuItemAcercaDeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAcercaDeActionPerformed
        AboutDialogo ad=new AboutDialogo(this,true);
        ad.setApplicationName(APPNAME);
        ad.setVersion(VERSION);
        ad.setYear("2010");
        ad.setAuthor("Lorenzo Carbonell");
        ad.setEmail("lorenzo.carbonell.cerezo@gmail.com");
        String texto="" +
                "Los gigantes y cabezudos son una tradición popular celebrada en " +
                "muchas fiestas locales de Europa occidental y América Latina. La " +
                "tradición consiste en hacer desfilar ciertas figuras bailando y " +
                "animando, los gigantes, o persiguiendo a la gente que acude a la " +
                "celebración, los cabezudos.\n\n"+
                "Los gigantes son figuras de varios metros de altura portados por " +
                "una persona. El portador hace girar y bailar el gigante al son de " +
                "una banda popular de música. Generalmente los gigantes desfilan " +
                "en parejas de gigante y giganta. Lo más habitual es que las figuras " +
                "representan arquetipos populares o figuras históricas de relevancia local.\n\n"+
                "Las figuras están realizadas en cartón-piedra , poliéster o fibra de vidrio " +
                "con un armazón de madera, hierro o aluminio que se cubre con amplios ropajes. " +
                "Los \"gigantes\" (llamados gigantones en parte de España y " +
                "gigantillas en Santander) tienen una altura desproporcionada, " +
                "creando un efecto de nobleza, mientras que en los \"cabezudos\" " +
                "(también llamados kilikis), de menor altura, se destaca la proporción " +
                "de la cabeza, dando un efecto más cómico. También existen los \"caballitos\" " +
                "(llamados \"zaldikos\" en Navarra), personajes mitad caballo " +
                "(la cabeza, parte del disfraz) mitad humano (resto del cuerpo).\n\n"+
                "Los desfiles de gigantes y cabezudos se suelen organizar en comparsas.";
        ad.setTexto(texto);
        ad.setImage("/es/atareao/caps/img/fondo_acercade.png");
        ad.setVisible(true);
}//GEN-LAST:event_jMenuItemAcercaDeActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        this.doAction(AC_NUEVO);
}//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        this.doAction(AC_ABRIR);
}//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        this.doAction(AC_GUARDAR);
}//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        this.doAction(AC_CALCULA);
}//GEN-LAST:event_jButton4ActionPerformed

    private void jPmaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPmaxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jPmaxActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        this.doAction(AC_PRINTPREVIEW);
    }//GEN-LAST:event_jButton5ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private es.atareao.alejandria.gui.JNumericField jDi;
    private es.atareao.alejandria.gui.JNumericField jDiametro;
    private es.atareao.alejandria.gui.JImage jImage1;
    private es.atareao.alejandria.gui.JImage jImage10;
    private es.atareao.alejandria.gui.JImage jImage11;
    private es.atareao.alejandria.gui.JImage jImage2;
    private es.atareao.alejandria.gui.JImage jImage3;
    private es.atareao.alejandria.gui.JImage jImage4;
    private es.atareao.alejandria.gui.JImage jImage5;
    private es.atareao.alejandria.gui.JImage jImage6;
    private es.atareao.alejandria.gui.JImage jImage7;
    private es.atareao.alejandria.gui.JImage jImage8;
    private es.atareao.alejandria.gui.JImage jImage9;
    private javax.swing.JComboBox jMaterial;
    private javax.swing.JMenu jMenuArchivo;
    private javax.swing.JMenuItem jMenuArchivoAbrir;
    private javax.swing.JMenuItem jMenuArchivoCerrar;
    private javax.swing.JMenuItem jMenuArchivoFile0;
    private javax.swing.JMenuItem jMenuArchivoFile1;
    private javax.swing.JMenuItem jMenuArchivoFile2;
    private javax.swing.JMenuItem jMenuArchivoFile3;
    private javax.swing.JMenuItem jMenuArchivoFile4;
    private javax.swing.JMenu jMenuArchivoFiles;
    private javax.swing.JMenuItem jMenuArchivoGuardar;
    private javax.swing.JMenuItem jMenuArchivoGuardarComo;
    private javax.swing.JMenuItem jMenuArchivoNuevo;
    private javax.swing.JMenu jMenuAyuda;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItemAcercaDe;
    private javax.swing.JMenuItem jMenuItemSalir;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private es.atareao.alejandria.gui.JNumericField jPmax;
    private es.atareao.alejandria.gui.JNumericField jPresion;
    private es.atareao.alejandria.gui.JNumericField jR;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JComboBox jTipo;
    private javax.swing.JToolBar jToolBar1;
    private es.atareao.alejandria.gui.JNumericField je;
    private es.atareao.alejandria.gui.JNumericField jh;
    private es.atareao.alejandria.gui.JNumericField jr;
    // End of variables declaration//GEN-END:variables
    private Preferencias _preferencias;
    private INIFile _datos;
    private boolean _opened=false;
    private ArrayList<String> _valores=new ArrayList<String>();

    private void setDefaultDir(File file){
        String str=file.toURI().toString();
        this.getPreferencias().setPreferencia("DefaultDir", str);
    }

    private File getDefaultDir(){
        String str=this.getPreferencias().getPreferencia("DefaultDir");
        if((str!=null)){
            try {
                URI uri = new URI(str);
                return new File(uri);
            }catch (IllegalArgumentException ex) {
                return FileUtils.getApplicationPath();
            }catch (URISyntaxException ex) {
                return FileUtils.getApplicationPath();
            }
        }
        return FileUtils.getApplicationPath();
    }
    /**
     * @return the _preferencias
     */
    public Preferencias getPreferencias() {
        return _preferencias;
    }

    /**
     * @param preferencias the _preferencias to set
     */
    public void setPreferencias(Preferencias preferencias) {
        this._preferencias = preferencias;
        String file0=preferencias.getPreferencia("File0");
        String file1=preferencias.getPreferencia("File1");
        String file2=preferencias.getPreferencia("File2");
        String file3=preferencias.getPreferencia("File3");
        String file4=preferencias.getPreferencia("File4");
        this.jMenuArchivoFile0.setText(file0);
        this.jMenuArchivoFile1.setText(file1);
        this.jMenuArchivoFile2.setText(file2);
        this.jMenuArchivoFile3.setText(file3);
        this.jMenuArchivoFile4.setText(file4);
    }

    /**
     * @return the _datos
     */
    public INIFile getDatos() {
        return _datos;
    }

    /**
     * @param datos the _datos to set
     */
    public void setDatos(INIFile datos) {
        this._datos = datos;
    }

    /**
     * @return the _opened
     */
    public boolean isOpened() {
        return _opened;
    }

    /**
     * @param opened the _opened to set
     */
    public void setOpened(boolean opened) {
        this._opened = opened;
    }
}

/*
 * Main
 *
 * File created on 01-feb-2010
 * Copyright (c) 2009 Lorenzo Carbonell
 * email: lorenzo.carbonell.cerezo@gmail.com
 * website: http://www.atareao.es
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.atareao.caps.main;

import es.atareao.alejandria.gui.ErrorDialog;
import es.atareao.alejandria.lib.FileUtils;
import es.atareao.alejandria.lib.INIFile;
import es.atareao.caps.gui.Caps;
import javax.swing.UIManager;

/**
 *
 * @author atareao
 */
public class Main {
    //
    //********************************CONSTANTES********************************
    //

    //
    // *********************************CAMPOS*********************************
    //

    //
    //******************************CONSTRUCTORES*******************************
    //

    //
    //********************************METODOS***********************************
    //
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String iniFileName=FileUtils.addPathFile(System.getProperty("user.dir"),"caps.ini").toString();
        INIFile iniFile=new INIFile(iniFileName);
        //
        try {
            UIManager.setLookAndFeel(iniFile.getStringProperty("Preferences","LookAndFeel"));
        } catch (Exception e) {
            ErrorDialog.manejaError(e,false);
        }
        //SplashScreen splashScreen = new SplashScreen ("/es/atareao/ferraplan/img/ferraplan_presen.png");
        //splashScreen.open(3000);
        //
        Caps caps=new Caps(iniFile);
        caps.setVisible(true);
    }
    //
    //**************************METODOS AUXILIARES******************************
    //

    //
    //**************************METODOS DE ACCESO*******************************
    //

}

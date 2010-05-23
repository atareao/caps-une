/*
 * PdfPage
 *
 * File created on 02-abr-2010
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

package es.atareao.caps.gui;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import java.awt.Image;
import java.awt.Rectangle;

/**
 *
 * @author atareao
 */

public class PdfPage {
    //
    //********************************CONSTANTES********************************
    //

    //
    // *********************************CAMPOS*********************************
    //
    private PDFFile _pdf;
    private int _page;
    private int _zoom;
    //
    //******************************CONSTRUCTORES*******************************
    //
    public PdfPage(PDFFile pdf,int page,int zoom){
        _pdf=pdf;
        _page=page;
        _zoom=zoom;
    }
    //
    //********************************METODOS***********************************
    //
    public Image getImage(){
        try{
            if((_page>0)&&(_page<=_pdf.getNumPages())){
                PDFPage page = _pdf.getPage(_page);
                //get the width and height for the doc at the default zoom
                float factor=((float)_zoom)/100;
                Rectangle rect = new Rectangle(0,0,(int)(page.getBBox().getWidth()),(int)(page.getBBox().getHeight()));
                //generate the image
                Image img = page.getImage(
                    (int)(rect.width*factor), (int)(rect.height*factor), //width & height
                    rect, // clip rect
                    null, // null for the ImageObserver
                    true, // fill background with white
                    true  // block until drawing is done
                );
                return img;
            }
        }catch(Exception ex){
            System.out.println(ex);
        }
        return null;
    }
    //
    //**************************METODOS AUXILIARES******************************
    //

    //
    //**************************METODOS DE ACCESO*******************************
    //
}

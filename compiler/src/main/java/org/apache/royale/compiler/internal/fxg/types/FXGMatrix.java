/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.royale.compiler.internal.fxg.types;

import org.apache.royale.compiler.internal.fxg.dom.transforms.MatrixNode;

import org.apache.royale.swf.ISWFConstants;
import org.apache.royale.swf.types.Matrix;

/**
 * Utility class to help with matrix transformation for coordinate transformation.
 */
public class FXGMatrix
{

	public double a; //x-axis scaling
    public double b; //x-axis skew
    public double c; //y-axis skew
    public double d; //y-axis scaling
    public double tx; //x-axis translation
    public double ty; //y-axis translation
    
    //constructor
    public FXGMatrix(double a, double b, double c, double d, double tx, double ty)
    {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.tx = tx;
        this.ty = ty;
    }
    
    //constructor - intializes matrix to identity matrix
    public FXGMatrix()
    {
        this.identity();
    }

    public FXGMatrix(MatrixNode m)
    {
        this.a = m.a;
        this.b = m.b;
        this.c = m.c;
        this.d = m.d;
        this.tx = m.tx;
        this.ty = m.ty;
    }

    public void identity() 
    {
        this.a = 1;
        this.b = 0;
        this.c = 0;
        this.d = 1;
        this.tx = 0;
        this.ty = 0;        
    }
    
    //concatenates matrix m to the current matrix
    public void concat(FXGMatrix m)
    {
        // Matrix multiplication 
        double new_a = a * m.a + b * m.c;
        double new_b = a * m.b + b * m.d;
        double new_c = c * m.a + d * m.c;
        double new_d = c * m.b + d * m.d;
        double new_tx = tx * m.a + ty * m.c + m.tx;
        double new_ty = tx * m.b + ty * m.d + m.ty;

        a  = new_a;
        b  = new_b;
        c  = new_c;
        d  = new_d;
        tx = new_tx;
        ty = new_ty;        
    }
    
    //concatenates a rotation matrix with rotation angle to the current matrix
    public void rotate(double angle)
    {
        double cos = Math.cos(angle*Math.PI/180.0);
        double sin = Math.sin(angle*Math.PI/180.0);
        FXGMatrix newM = new FXGMatrix(cos, sin, -sin, cos, 0, 0);
        this.concat (newM);
    }
    
    //concatenates a scaling matrix with scale factors scaleX and scaleY to the current matrix
    public void scale(double scaleX, double scaleY)
    {
        FXGMatrix newM = new FXGMatrix(scaleX, 0, 0, scaleY, 0, 0);
        this.concat (newM);     
    }
    
    //concatenates a transaltion matrix with translations (dx, dy) to the current matrix
    public void translate(double dx, double dy)
    {
        tx += dx;
        ty += dy;
    }
    
    //creates a matrix from the discrete transform parameters
    public static FXGMatrix convertToMatrix(double scaleX, double scaleY, double rotation, double tx, double ty)
    {
        FXGMatrix m = new FXGMatrix();
        m.scale (scaleX, scaleY);
        m.rotate (rotation);
        m.translate(tx, ty);        
        return m;
    }

    //returns a SWF Matrix data type that is equivalent to the current matrix
    public Matrix toSWFMatrix()
    {
        
        /*SWF matrices need to be invertible - check if it is invertible
         * disabled it for now - other apps seem to allow it
        FXGMatrix newm = new FXGMatrix(a, b, c, d, tx, ty);
        if (!newm.invert())
            throw new FXGException("MatrixNotInvertible");
        */
        
        Matrix sm = new Matrix();
        if (b != 0 || c != 0)
            sm.setRotate(b, c);        
        if (a != 0 || d != 0)
            sm.setScale(a, d);
        
        sm.setTranslate((int)(tx*ISWFConstants.TWIPS_PER_PIXEL), (int)(ty*ISWFConstants.TWIPS_PER_PIXEL));
        
        return sm;        
    }
    
    /**
     * Set matrix attribute values with values in this FXGMatrix object.
     * @param node - the matrix node whose attribute values will be updated.
     */
    public void setMatrixNodeValue(MatrixNode node)
    {
        node.a = this.a;
        node.b = this.b;
        node.c = this.c;
        node.d = this.d;
        node.tx = this.tx;
        node.ty = this.ty;
    }
    
}

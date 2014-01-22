package de.fau.spicsim.gui;

/**
 * SiebenSegmentAnzeige
 * by Christian Götz
 * 27.02.2007
 * 
 * 
 * Eine Siebensegmentanzeige in Form einer Swingkomponente  
 * Das ideale Größenverhältnis der Anzeige ist width/height = 12/20
 * http://www.java-forum.org/allgemeine-java-themen/42646-7-segment-anzeige.html
 */




import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.*;



public class SevenSeg extends Component {
	
	private static final long serialVersionUID = 7364720260424088546L;

	public static final float MIN_WIDTH = 12, MIN_HEIGHT = 20;
	
	
	private Color[] segment = {null, null, null, null, null, null, null};
	
	private boolean paintSegmentBorder = true;
	private Color borderColor = Color.GRAY;
	
	
	public SevenSeg()
	{
		super();
		init();
	}


	private void init()
	{
		setForeground(Color.RED);
		setSize((int)MIN_WIDTH, (int)MIN_HEIGHT);
		setPreferredSize(new Dimension((int)MIN_WIDTH*2, (int)MIN_HEIGHT*2));
		setMinimumSize(new Dimension((int)MIN_WIDTH, (int)MIN_HEIGHT));		
	}
	
	
	/**Malt einen Rahmen um die einzelnen Segmente. Die Farbe kann mit setSegmentBorderColor(Color c) eingestellt werden. 
	 * 
	 * @param b
	 */
	public void paintSegmentBorder(boolean b)
	{
		paintSegmentBorder = b;
	}
	
	
	/** gibt true zurück wenn ein Rahmen um die Segmente gezeichnet wird. 
	 * 
	 * @return
	 */
	public boolean isPaintingSegmentBorder()
	{
		return paintSegmentBorder;
	}
	
	
	/** gibt die Farbe des Segmentrahmen zurück.
	 * 
	 * @return  die Farbe des Segmentrahmen.
	 */ 
	public Color getSegmentBorderColor()
	{
		return borderColor;
	}
	
	/** setzt die Farbe des Segmentrahmen.
	 * 
	 * @param c  die Farbe des Segmentrahmen.
	 */
	public void setSegmentBorderColor(Color c)
	{
		borderColor = c;		
		repaint();
	}
	

	/** schaltet ein einzelnes Segment (0-6) ein oder aus
	 * 
	 * @param segment  das Segment das geschaltet werden soll
	 * @param state  true schaltet das Segment ein, false aus.
	 */	 
	public void setSegment(int segment, Color state)
	{
		if(segment < 0) segment = 0;
		else if(segment > 6) segment = 6;
		
		this.segment[segment] = state;
		
		repaint();
	}
	
	/** schaltet alle 7 Segmente an oder aus.
	 * 
	 * @param state  true schaltet die Segmente ein, false aus.
	 */
	public void setAllSegments(Color state)
	{
		for (int i=0 ; i<7 ; i++)
		{
			segment[i] = state;
		}
		repaint();
	}
	

	@Override
	public void paint(Graphics gr)
	{
		Graphics2D g = (Graphics2D) gr;
		g.setBackground(Color.black);
		g.clearRect(0, 0, getWidth(), getHeight());
		
		for(int i=0 ; i<segment.length ; i++)
		{
			paintSegment(g, i, segment[i]);
		}
	}
	
	private void paintSegment(Graphics2D g, int segment, Color col)
	{
		
		switch(segment)
		{			
//			Horizontale Segmente:
			case 0:	
				paintHorizontalSegment(g, 0, col);
			break;	
			
			case 6:
				paintHorizontalSegment(g, 8, col);
			break;	
				
			case 3:
				paintHorizontalSegment(g, 16, col);
			break;	
			
//			Vertikale Segmente:	
			case 5:
				paintVerticalSegment(g, 0, 0, col);
			break;
				
			case 1:
				paintVerticalSegment(g, 0, 8, col);
			break;
				
			case 4:
				paintVerticalSegment(g, 8, 0, col);
			break;
				
			case 2:
				paintVerticalSegment(g, 8, 8, col);
			break;
		}
	}
	
	private void paintHorizontalSegment(Graphics2D g, int row, Color col)
	{
		float fieldWidth = (getWidth())/12f;
		float fieldHeight = (getHeight())/20f;		
		
		
		if(col != null){
			Path2D.Float polygon1 = new Path2D.Float();
			Path2D.Float polygon2 = new Path2D.Float();		
			
			Rectangle2D.Float rect = new Rectangle2D.Float();
			
			polygon1.moveTo(fieldWidth*2, fieldHeight*2 + row*fieldHeight);
			polygon1.lineTo(fieldWidth*3, fieldHeight + row*fieldHeight);
			polygon1.lineTo(fieldWidth*3, fieldHeight*3 + row*fieldHeight);
			polygon1.lineTo(fieldWidth*2, fieldHeight*2 + row*fieldHeight);
					
			rect.setRect(fieldWidth*3, fieldHeight + row*fieldHeight, 
					fieldWidth*6, fieldHeight*2);	
			
			polygon2.moveTo(fieldWidth*9, fieldHeight + row*fieldHeight);
			polygon2.lineTo(fieldWidth*10, fieldHeight*2 + row*fieldHeight);
			polygon2.lineTo(fieldWidth*9, fieldHeight*3 + row*fieldHeight);
			polygon2.lineTo(fieldWidth*9, fieldHeight + row*fieldHeight);
			
			
			g.setPaint(col);
			g.fill(polygon1);
			g.fill(rect);
			g.fill(polygon2);
		}
		
		if(paintSegmentBorder)
		{
			Path2D.Float border = new Path2D.Float();		
			border.moveTo(fieldWidth*2, fieldHeight*2 + row*fieldHeight);
			border.lineTo(fieldWidth*3, fieldHeight + row*fieldHeight);
			border.lineTo(fieldWidth*9, fieldHeight + row*fieldHeight);
			border.lineTo(fieldWidth*10, fieldHeight*2 + row*fieldHeight);
			border.lineTo(fieldWidth*9, fieldHeight*3 + row*fieldHeight);
			border.lineTo(fieldWidth*3, fieldHeight*3 + row*fieldHeight);
			border.lineTo(fieldWidth*2, fieldHeight*2 + row*fieldHeight);
			
			g.setPaint(borderColor);
			g.draw(border);
		}
	}
	
	private void paintVerticalSegment(Graphics2D g, int row, int column, Color col)
	{		
		float fieldWidth = (getWidth())/12f;
		float fieldHeight = (getHeight())/20f;		
		
		if(col != null){
			Path2D.Float polygon1 = new Path2D.Float();
			Path2D.Float polygon2 = new Path2D.Float();		
			
			Rectangle2D.Float rect = new Rectangle2D.Float();
			
			
			polygon1.moveTo(fieldWidth*2 + column*fieldWidth, fieldHeight*2 + row*fieldHeight);
			polygon1.lineTo(fieldWidth*3 + column*fieldWidth, fieldHeight*3 + row*fieldHeight);
			polygon1.lineTo(fieldWidth + column*fieldWidth, fieldHeight*3 + row*fieldHeight);
			polygon1.lineTo(fieldWidth*2 + column*fieldWidth, fieldHeight*2 + row*fieldHeight);
					
			rect.setRect(fieldWidth + column*fieldWidth, fieldHeight*3 + row*fieldHeight, 
					fieldWidth*2, fieldHeight*6);	
			
			polygon2.moveTo(fieldWidth + column*fieldWidth, fieldHeight*9 + row*fieldHeight);
			polygon2.lineTo(fieldWidth*3 + column*fieldWidth, fieldHeight*9 + row*fieldHeight);
			polygon2.lineTo(fieldWidth*2 + column*fieldWidth, fieldHeight*10 + row*fieldHeight);
			polygon2.lineTo(fieldWidth + column*fieldWidth, fieldHeight*9 + row*fieldHeight);
			
			
			g.setPaint(col);
			g.fill(polygon1);
			g.fill(rect);
			g.fill(polygon2);
		}
		
		
		if(paintSegmentBorder)
		{
			Path2D.Float border = new Path2D.Float();
			border.moveTo(fieldWidth*2 + column*fieldWidth, fieldHeight*2 + row*fieldHeight);
			border.lineTo(fieldWidth*3 + column*fieldWidth, fieldHeight*3 + row*fieldHeight);
			border.lineTo(fieldWidth*3 + column*fieldWidth, fieldHeight*9 + row*fieldHeight);
			border.lineTo(fieldWidth*2 + column*fieldWidth, fieldHeight*10 + row*fieldHeight);
			border.lineTo(fieldWidth + column*fieldWidth, fieldHeight*9 + row*fieldHeight);
			border.lineTo(fieldWidth + column*fieldWidth, fieldHeight*3 + row*fieldHeight);
			border.lineTo(fieldWidth*2 + column*fieldWidth, fieldHeight*2 + row*fieldHeight);
			
			g.setPaint(borderColor);
			g.draw(border);			
		}

	}
	

}
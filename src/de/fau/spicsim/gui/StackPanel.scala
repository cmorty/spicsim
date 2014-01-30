package de.fau.spicsim.gui

import java.awt.Font
import javax.swing.JFrame
import java.awt.GridLayout
import javax.swing.JPanel
import javax.swing.JLabel
import info.monitorenter.gui.chart.Chart2D
import info.monitorenter.gui.chart.ITrace2D
import info.monitorenter.gui.chart.traces.Trace2DLtd
import info.monitorenter.gui.chart.views.ChartPanel
import java.awt.Color
import info.monitorenter.gui.chart.axis.AxisLinear
import info.monitorenter.gui.chart.labelformatters.LabelFormatterUnit
import info.monitorenter.util.units.UnitMilli
import info.monitorenter.gui.chart.IAxis
import info.monitorenter.gui.chart.IAxisScalePolicy
import info.monitorenter.gui.chart.axis.scalepolicy._


class StackPanel extends JFrame {

	class XScalePol extends AxisScalePolicyAutomaticBestFit {
		import info.monitorenter.gui.chart.LabeledValue
		override protected def roundToTicks(value: Double, floor: Boolean, findMajorTick: Boolean, axis: IAxis[_]) = {
			val ret = new LabeledValue();
			val minorTick = axis.getMinorTickSpacing() * this.m_power;
			val majorTick = axis.getMajorTickSpacing() * this.m_power;
			var majorRound = if (floor) {
				Math.floor(value / majorTick);
			} else {
				Math.ceil(value / majorTick);
			}
			val majorZeroHit = (majorRound == 0) && (value != 0);
			majorRound *= majorTick;
			
			
			/*

    double minorRound;
    if (floor) {
      minorRound = Math.floor(value / minorTick);
    } else {
      minorRound = Math.ceil(value / minorTick);
    }
    final boolean minorZeroHit = (minorRound == 0) && (value != 0);
    minorRound *= minorTick;
    if (majorZeroHit || minorZeroHit) {
      if (AAxis.DEBUG) {
        System.out.println("zeroHit");
      }
    }

    final double minorDistance = Math.abs(value - minorRound);
    final double majorDistance = Math.abs(value - majorRound);

    double majorMinorRelation = minorDistance / majorDistance;
    if (Double.isNaN(majorMinorRelation)) {
      majorMinorRelation = 1.0;
    }

    if ((majorDistance <= minorDistance) || findMajorTick) {
      ret.setValue(majorRound);
      ret.setMajorTick(true);
    } else {
      ret.setValue(minorRound);
      ret.setMajorTick(false);
    }

    // format label string.
    ret.setLabel(axis.getFormatter().format(ret.getValue()));
    // as formatting rounds too, reparse value so that it is exactly at the
    // point the label string describes.
    ret.setValue(axis.getFormatter().parse(ret.getLabel()).doubleValue());*/
			ret;
		}
	}
	
	//Panel
	setBounds(100, 100, 450, 300);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	getContentPane().setLayout(null);
	setLayout(new GridLayout(1, 2));
	
	//Chart
	val chart = new Chart2D
	
	val trace = new Trace2DLtd(100) 
	trace.setColor(Color.RED)
	chart.addTrace(trace)
	
	
    val xAxis = chart.getAxisX().asInstanceOf[IAxis[IAxisScalePolicy]]
	xAxis.setAxisScalePolicy(new AxisScalePolicyAutomaticBestFit()); 

    xAxis.setMajorTickSpacing(50);
    xAxis.setMinorTickSpacing(5);
    xAxis.setStartMajorTick(false);
    
    
    val yAxis = chart.getAxisY().asInstanceOf[IAxis[IAxisScalePolicy]]
    yAxis.setAxisScalePolicy(new AxisScalePolicyManualTicks()); 
    yAxis.setMajorTickSpacing(10);
    yAxis.setMinorTickSpacing(1);
    yAxis.setStartMajorTick(false);
    
    
    
    // Set a date formatter:
    
    //chart.setAxisYLeft(yAxis);
	
	
	
	add(chart)
	
	
	def addVal(ts:Long, value:Long){
		trace.addPoint(ts.toDouble/1000,value )
	}
	setVisible(true)

}
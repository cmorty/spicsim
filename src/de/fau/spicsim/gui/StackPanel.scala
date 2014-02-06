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
import de.fau.spicsim.interfaces.StackMonListener

class StackPanel extends JFrame with StackMonListener {

	def addVal(timeStamp: Long, stackSize: Long) {
		trace.addPoint(timeStamp.toDouble / 1000, stackSize)
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

	setVisible(true)
}
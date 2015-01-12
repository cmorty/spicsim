package de.fau.spicsim

import java.awt.EventQueue
import de.fau.spicsim.gui.Gui
import java.io.File
import de.fau.spicsim.text.TextOutput

case class Config(
	val file: File = null)

object Main {

	var gui: Gui = null;

	val sim = new SpicSim

	def main(args: Array[String]): Unit = {

		val a = new Config

		val parser = new scopt.OptionParser[Config]("scopt") {
			head("scopt", "3.x")
			arg[File]("<file>")
				.optional()
				.action { (x, c) => c.copy(file = x) }
				.text("File to simulate.")
				.validate { x => if (x.exists()) success else failure("File not found") }
		}

		val conf = parser.parse(args, Config()).getOrElse(sys.exit(1))

		initGui(sim)
		new TextOutput(sim, System.out)
		if (conf.file != null) {
			sim.load(conf.file)
			sim.start
		}

	}

	def initGui(ssim: SpicSim) {
		EventQueue.invokeLater(new Runnable() {
			def run() {

			}
		});

		gui = new Gui(ssim)

	}

}
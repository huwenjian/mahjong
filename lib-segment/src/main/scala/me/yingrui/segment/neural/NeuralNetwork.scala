package me.yingrui.segment.neural

import me.yingrui.segment.math.Matrix
import me.yingrui.segment.neural.Layer
import me.yingrui.segment.util.{FileUtil, SerializeHandler}

class NeuralNetwork {

  var layers = List[Layer]()

  def add(layer: Layer) {
    assert(layers.length == 0 || layers.last.weight.col == layer.weight.row)
    layers = layers :+ layer
  }

  def computeOutput(input: Matrix): Matrix = {
    layers.foldLeft(input)((inputVertex, layer) => layer.computeOutput(inputVertex))
  }

  override def toString(): String = layers.map(layer => Array(layer.weight, layer.bias)).flatten.mkString("\n\n")

  def save(file: String) {
    val dumper = SerializeHandler(new java.io.File(file), SerializeHandler.WRITE_ONLY)
    dumper.serializeInt(layers.length)
    layers.foreach(layer => {
      dumper.serializeMatrix(layer.weight)
      dumper.serializeMatrix(layer.bias)
    })
    dumper.close()
  }

  def load(resource: String) {
    val input = new java.io.DataInputStream(FileUtil.getResourceAsStream(resource))
    val serializeHandler = new SerializeHandler(input, null)
    for(i <- 0 until serializeHandler.deserializeInt()) {
      val weight: Matrix = serializeHandler.deserializeMatrix()
      val bias: Matrix = serializeHandler.deserializeMatrix()
      add(SigmoidLayer(weight, bias))
    }
    input.close()
  }
}

object NeuralNetwork {

  def apply() = new NeuralNetwork()
}
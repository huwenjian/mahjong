package me.yingrui.segment.neural.regression

import java.lang.Math.abs

import me.yingrui.segment.math.Matrix
import me.yingrui.segment.neural._
import me.yingrui.segment.neural.errors.RMSLoss
import me.yingrui.segment.util.Logger
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class SigmoidRegressionTest extends FunSuite with Matchers with BeforeAndAfter with WisconsinBreastCancerDataSet {

  private def train = {
    val numberOfClasses = 2

    val numberOfFeatures = trainDataSet(0)._1.col

    val weight = Matrix.randomize(numberOfFeatures, numberOfClasses, -1D, 1D)
    val bias = Matrix.randomize(1, numberOfClasses, -1D, 1D)
    var cost = 0D

    def calculateGrad(trainSet: Seq[(Matrix, Matrix)], weight: Matrix, bias: Matrix): Unit = {
      val error = new RMSLoss()

      val layer = new BPSigmoidLayer(weight, bias, false)

      trainSet.foreach(data => {
        val output: Matrix = layer.computeOutput(data._1)
        val expectedOutput: Matrix = data._2
        error.updateError(output, expectedOutput)
        val err = expectedOutput - output
        layer.propagateError(err)
        layer.update(1.0D, 0.0D)
      })

      cost = error.loss
    }

    var iteration = 0
    var lastCost = 0.0D
    var hasImprovement = true
    while (iteration < 100000 && hasImprovement) {
      calculateGrad(trainDataSet, weight, bias)
      Logger.debug(s"iter: ${iteration} cost: ${cost}")
      hasImprovement = abs(cost - lastCost) > 1e-5

      lastCost = cost
      iteration += 1
    }

    new BPSigmoidLayer(weight, bias, false)
  }

  private def classify(classifier: Layer, testInput: Matrix): Matrix = {
    val actualOutput = classifier computeOutput testInput
    if (actualOutput(0, 0) > actualOutput(0, 1)) {
      actualOutput(0, 0) = 1.0D
      actualOutput(0, 1) = 0.0D
    } else {
      actualOutput(0, 0) = 0.0D
      actualOutput(0, 1) = 1.0D
    }
    actualOutput
  }

  test("train sigmoid classifier") {
    val tic = System.currentTimeMillis()
    println("train set contains " + trainDataSet.size + " samples, test set contains " + testDataSet.size + " samples.")

    val layer = train

    val error = testDataSet.map(data => {
      val testInput = data._1
      val expectedOutput = data._2

      val actualOutput: Matrix = classify(layer, testInput)

      if((expectedOutput - actualOutput).map(abs(_)).sum > 0)
        1.0D
      else
        0.0D
    }).sum

    val numberOfSamples = testDataSet.size.toDouble
    val accuracy = 1.0 - error / numberOfSamples
    println("error = " + error + " total = " + numberOfSamples)
    println("accuracy = " + accuracy)
    accuracy should be > 0.92
    val toc = System.currentTimeMillis()
    val second = (toc - tic) / 1000
    println(s"elapsed time: ${second}s")
  }


}

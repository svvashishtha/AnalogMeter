package com.sample.karmameter

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


class KarmaMeter @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(ctx, attrs, defStyleAttr) {


    var paintInnerArc: Paint = Paint()
    var needlePaint: Paint = Paint()
    var needleStrokePaint: Paint = Paint()
    var divisionPaint: Paint = Paint()
    var textPaint: Paint = Paint()
    var rectOuter: RectF? = null
    var rectInner: RectF? = null
    private val useCenter = false
    var needlePath = Path()
    var divisionPath = Path()
    private var divisionPathForArc = Path()
    private val textPath = Path()

    var divisionPointsStart: ArrayList<Point> = ArrayList()
    var divisionPointsEnd: ArrayList<Point> = ArrayList()
    var divisionPointsForArc: ArrayList<Point> = ArrayList()
    var pathsForText: ArrayList<Path> = ArrayList()
    var pathsForDivs: ArrayList<Path> = ArrayList()
    var paintForDivs: ArrayList<Paint> = ArrayList()
    var divisionStrokeWidth = 5F
    var needleStrokeWidth = 10F
    val outerRingThicknessRatio = 0.1
    val textouterRingThicknessRatio = 0.075
    val needleHeightRatio = 0.2
    val radiusOfTopArc = 5
    val radiusOfBottomArc = 2 * radiusOfTopArc
    val topSideOfRectangle = 2 * radiusOfTopArc
    val noOfDivisions = 5
    val divisionMarginRatioUpper = 0.95
    val divisionMarginRatioLower = 0.9

    var innerArcColor = R.color.white
    var textColor = R.color.grey
    var selectedTextColor = R.color.white
    var needleColor = R.color.grey
    var needleStrokeColor = R.color.white
    var divisionColor = R.color.transparent
    var levelType = LEVEL_TYPE.NUMBERED
    var numberString = "1 "
    val colors = arrayOf(R.color.red,R.color.orange, R.color.yellow, R.color.light_green,R.color.dark_green, R.color.red, R.color.yellow  )
    init {
        context?.let {

            for (j in 0 until noOfDivisions)
            {
                var paintOuterArc = Paint()
                paintOuterArc.color = ContextCompat.getColor(context, colors[j])
                paintOuterArc.style = Paint.Style.FILL
                paintForDivs.add(paintOuterArc)
                if (j > 0)
                {
                    numberString += (j+1).toString() +" "
                }
            }
            paintInnerArc.color = ContextCompat.getColor(context, innerArcColor)
            divisionPaint.strokeWidth = divisionStrokeWidth
            needlePaint.color = ContextCompat.getColor(context, needleColor)
            needlePaint.style = Paint.Style.FILL
            needlePaint.isAntiAlias = true
            needleStrokePaint.color = ContextCompat.getColor(context, needleStrokeColor)
            needleStrokePaint.style = Paint.Style.STROKE
            needleStrokePaint.strokeWidth = needleStrokeWidth
            needleStrokePaint.isAntiAlias = true
            divisionPaint.color = ContextCompat.getColor(context, divisionColor)
            divisionPaint.style = Paint.Style.STROKE
            divisionPaint.strokeWidth = divisionStrokeWidth
            textPaint.color = textColor
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 50F
        }

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { canvas ->

            pathsForDivs.forEachIndexed { index, path ->
                canvas.drawPath(path,paintForDivs[index])
            }

            rectInner?.let { rectInner ->
                canvas.drawArc(rectInner, 180F, 180F, useCenter, paintInnerArc)
            }
            canvas.drawPath(needlePath, needleStrokePaint)
            canvas.drawPath(needlePath, needlePaint)
            canvas.drawPath(divisionPath, divisionPaint)

            numberString.split(" ").forEachIndexed { index, letter ->
                canvas.drawTextOnPath(letter, pathsForText[index], 0F, 0F, textPaint)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        var height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        if (height == 0) height = width
        if (width == 0) width = height
        recalculateArcRects(width, height)
        calculateDivisionPath(width / 2)
        calculatePathsForOuterArc(width, height)
        calculatePathForNeedle(width, (height / 2.5).roundToInt())
        setMeasuredDimension(width, height / 2)
    }

    private fun calculatePathsForOuterArc(width: Int, height: Int) {
        val angle = 180 / noOfDivisions

        val rectOuter = RectF(
            0F,
            0F,
            (height).toFloat(),
            (width).toFloat()
        )
        val textRect = RectF(
            (width * textouterRingThicknessRatio).toFloat(),
            (height * textouterRingThicknessRatio).toFloat(),
            (height * (1 - textouterRingThicknessRatio)).toFloat(),
            (width * (1 - textouterRingThicknessRatio)).toFloat()
        )
        var lowerPointArithmeticOffset = noOfDivisions / 2

        pathsForDivs = ArrayList()
        for (j in 1..noOfDivisions) {
            divisionPathForArc = Path()
            //add the outer arc. This will move from base towards the top.
            divisionPathForArc.arcTo(rectOuter, 180F + (j - 1) * angle.toFloat(), angle.toFloat())
            when (j) {
                1 -> {
                    //add line towards the inner arc
                    divisionPathForArc.lineTo(
                        divisionPointsForArc[j].x.toFloat(), divisionPointsForArc[j].y.toFloat()
                    )

                    //add line to inner circle's bottom
                    divisionPathForArc.lineTo(
                        width * outerRingThicknessRatio.toFloat(),
                        (width / 2).toFloat()
                    )
                }
                noOfDivisions -> {
                    //add line towards the inner arc
                    divisionPathForArc.lineTo(
                        width * (1 - outerRingThicknessRatio.toFloat()),
                        (width / 2).toFloat()
                    )
                    //add line to the top of arc on the inner circle along the inner circle
                    divisionPathForArc.lineTo(
                        divisionPointsForArc[j - lowerPointArithmeticOffset].x.toFloat(),
                        divisionPointsForArc[j - lowerPointArithmeticOffset].y.toFloat()
                    )
                }
                else -> {
                    //this is for 2
                    //add line towards the inner arc
                    divisionPathForArc.lineTo(
                        divisionPointsForArc[2 * j - 1].x.toFloat(),
                        divisionPointsForArc[2 * j - 1].y.toFloat()
                    )
                    //add line to the base of arc on the inner circle along the inner circle
                    divisionPathForArc.lineTo(
                        divisionPointsForArc[j - lowerPointArithmeticOffset].x.toFloat(),
                        divisionPointsForArc[j - lowerPointArithmeticOffset].y.toFloat()
                    )

                }
            }
            lowerPointArithmeticOffset -= 1

            //tell path to close
            divisionPathForArc.close()

            pathsForDivs.add(divisionPathForArc)

            val path = Path()
            path.addArc(textRect, 180F + (j - 1) * angle.toFloat(), angle.toFloat())
            pathsForText.add(path)
        }


    }

    public fun rotate(angle: Float) {

        val mMatrix = Matrix()
        needlePivotPoint?.let { mMatrix.postRotate(angle, it.x.toFloat(), it.y.toFloat()) }
        needlePath.transform(mMatrix)
        invalidate()
    }

    public fun setLevelingType(levelType: LEVEL_TYPE) {
        this.levelType = levelType
        invalidate()

    }

    private fun calculateDivisionPath(radius: Int) {

        val angle = 180 / noOfDivisions
        divisionPointsStart = ArrayList()

        divisionPointsEnd = ArrayList()
        val centerPointOfView = Point(radius, radius)
        for (i in 1 until noOfDivisions) {
            var angleAdjusted = i * angle
            var multiplier = -1
            if (angleAdjusted > 90) {
                angleAdjusted = 180 - angleAdjusted
                multiplier *= -1
            }

            var startX = radius * cos((angleAdjusted * Math.PI / 180))
            var startY = radius * sin((angleAdjusted * Math.PI / 180))


            var startXNew = startX * divisionMarginRatioUpper
            var startYNew = startY * divisionMarginRatioUpper


            startXNew = centerPointOfView.x + multiplier * startXNew

            startYNew = centerPointOfView.y - startYNew

            divisionPointsForArc.add(
                Point(
                    (centerPointOfView.x + multiplier * startX).toInt(),
                    (centerPointOfView.y - startY).toInt()
                )
            )


            divisionPointsStart.add(Point(startXNew.toInt(), startYNew.toInt()))

            val radiusNew = (radius * divisionMarginRatioLower).toInt()
            var endX = radiusNew * divisionMarginRatioUpper * cos((angleAdjusted * Math.PI / 180))
            var endY = radiusNew * divisionMarginRatioUpper * sin((angleAdjusted * Math.PI / 180))

            divisionPointsForArc.add(
                Point(
                    (centerPointOfView.x + multiplier * radiusNew * divisionMarginRatioLower * cos((angleAdjusted * Math.PI / 180))).toInt(),
                    (centerPointOfView.y - radiusNew * divisionMarginRatioLower * sin((angleAdjusted * Math.PI / 180))).toInt()
                )
            )

            endX = centerPointOfView.x + multiplier * endX

            endY = centerPointOfView.y - endY

            divisionPointsEnd.add(Point(endX.toInt(), endY.toInt()))
        }
        divisionPointsStart.forEachIndexed { index, it ->
            divisionPath.moveTo(
                divisionPointsEnd[index].x.toFloat(),
                divisionPointsEnd[index].y.toFloat()
            )
            divisionPath.lineTo(it.x.toFloat(), it.y.toFloat())


        }


    }

    var centerPoint: Point? = null
    var needlePivotPoint: Point? = null

    private fun calculatePathForNeedle(width: Int, height: Int) {

        centerPoint = Point(width / 2, 0)
        centerPoint?.let {


            //add top arc
            needlePath.addArc(
                /* left =*/ (it.x - radiusOfTopArc).toFloat(),
                /*top =*/(it.y + height * needleHeightRatio).toFloat(),
                /*right =*/(it.x + radiusOfTopArc).toFloat(),
                /*bottom =*/(it.y + 2 * radiusOfTopArc + height * needleHeightRatio).toFloat(),
                /*startAngle =*/180F,
                /*sweepAngle =*/ 180F
            )

            //add rectangle
            needlePath.moveTo(
                (it.x - topSideOfRectangle / 2).toFloat(),
                (it.y + topSideOfRectangle / 2 + height * needleHeightRatio).toFloat()
            )
            needlePath.lineTo(
                (it.x + topSideOfRectangle / 2).toFloat(),
                (it.y + topSideOfRectangle / 2 + height * needleHeightRatio).toFloat()
            )
            needlePath.lineTo(
                (it.x + topSideOfRectangle).toFloat(),
                height.toFloat() - topSideOfRectangle
            )
            needlePath.lineTo(
                (it.x - topSideOfRectangle).toFloat(),
                (height - topSideOfRectangle).toFloat()
            )
            needlePath.lineTo(
                (it.x - topSideOfRectangle / 2).toFloat(),
                (it.y + topSideOfRectangle / 2 + height * needleHeightRatio).toFloat()
            )


            //add bottom arc
            needlePath.lineTo(
                (it.x - radiusOfBottomArc).toFloat(),
                height.toFloat() - radiusOfBottomArc
            )
            needlePath.addArc(
                /* left =*/ (it.x - radiusOfBottomArc).toFloat(),
                /*top =*/height.toFloat() - 2 * radiusOfBottomArc,
                /*right =*/(it.x + radiusOfBottomArc).toFloat(),
                /*bottom =*/height.toFloat(),
                /*startAngle =*/0F,
                /*sweepAngle =*/ 180F
            )

            val rect = Rect(
                it.x - radiusOfBottomArc,
                /*top =*/height - 2 * radiusOfBottomArc,
                /*right =*/(it.x + radiusOfBottomArc),
                /*bottom =*/height
            )
            needlePivotPoint = Point(rect.centerX(), rect.centerY())

            needlePath.close()


            //add the stroke


        }
    }

    private fun recalculateArcRects(width: Int, height: Int) {

        rectOuter = RectF(0.0F, 0.0F, height.toFloat(), width.toFloat())
        rectInner = RectF(
            (width * outerRingThicknessRatio).toFloat(),
            (height * outerRingThicknessRatio).toFloat(),
            (height * (1 - outerRingThicknessRatio)).toFloat(),
            (width * (1 - outerRingThicknessRatio)).toFloat()
        )
        textPath.addArc(rectInner, 198F, 144F)
    }

}

enum class LEVEL_TYPE {
    NUMBERED,
    CUSTOM
}
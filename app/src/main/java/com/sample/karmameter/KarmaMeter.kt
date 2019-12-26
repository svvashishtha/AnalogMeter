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
    var paintOuterArc1: Paint = Paint()
    var paintOuterArc2: Paint = Paint()
    var paintOuterArc3: Paint = Paint()
    var paintOuterArc4: Paint = Paint()
    var paintOuterArc5: Paint = Paint()
    var needlePaint: Paint = Paint()
    var needleStrokePaint: Paint = Paint()
    var divisionPaint: Paint = Paint()
    var textPaint: Paint = Paint()
    var rectOuter: RectF? = null
    var rectInner: RectF? = null
    private val useCenter = false
    var needlePath = Path()
    var divisionPath = Path()
    private val divisionPathForArc1 = Path()
    private val divisionPathForArc2 = Path()
    private val divisionPathForArc3 = Path()
    private val divisionPathForArc4 = Path()
    private val divisionPathForArc5 = Path()
    private val textPath = Path()

    var divisionPointsStart: ArrayList<Point> = ArrayList()
    var divisionPointsEnd: ArrayList<Point> = ArrayList()
    var divisionPointsForArc: ArrayList<Point> = ArrayList()
    var centerPointsForText: ArrayList<Point> = ArrayList()
    var divisionStrokeWidth = 5F
    var needleStrokeWidth = 10F
    val outerRingThicknessRatio = 0.1
    val needleHeightRatio = 0.2
    val radiusOfTopArc = 5
    val radiusOfBottomArc = 2 * radiusOfTopArc
    val topSideOfRectangle = 2 * radiusOfTopArc
    val noOfDivisions = 5
    val divisionMarginRatioUpper = 0.95
    val divisionMarginRatioLower = 0.9

    var innerArcColor = R.color.white
    var outerArcColor1 = R.color.red
    var outerArcColor2 = R.color.orange
    var outerArcColor3 = R.color.yellow
    var outerArcColor4 = R.color.light_green
    var outerArcColor5 = R.color.dark_green
    var textColor = R.color.grey
    var selectedTextColor = R.color.white
    var needleColor = R.color.grey
    var needleStrokeColor = R.color.white
    var divisionColor = R.color.transparent
    var levelType = LEVEL_TYPE.NUMBERED
    val numberString = "1 2 3 4 5"
    init {
        context?.let {
            paintInnerArc.color = ContextCompat.getColor(context, innerArcColor)
            paintOuterArc1.color = ContextCompat.getColor(context, outerArcColor1)
            paintOuterArc1.style = Paint.Style.FILL
            paintOuterArc2.color = ContextCompat.getColor(context, outerArcColor2)
            paintOuterArc2.style = Paint.Style.FILL
            paintOuterArc3.color = ContextCompat.getColor(context, outerArcColor3)
            paintOuterArc3.style = Paint.Style.FILL
            paintOuterArc4.color = ContextCompat.getColor(context, outerArcColor4)
            paintOuterArc4.style = Paint.Style.FILL
            paintOuterArc5.color = ContextCompat.getColor(context, outerArcColor5)
            paintOuterArc5.style = Paint.Style.FILL
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
            textPaint.style = Paint.Style.STROKE
            textPaint.textSize = 50F
            textPaint.letterSpacing = 1.5F
        }

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { canvas ->
            canvas.drawPath(divisionPathForArc1, paintOuterArc1)
            canvas.drawPath(divisionPathForArc2, paintOuterArc2)
            canvas.drawPath(divisionPathForArc3, paintOuterArc3)
            canvas.drawPath(divisionPathForArc4, paintOuterArc4)
            canvas.drawPath(divisionPathForArc5, paintOuterArc5)
            rectInner?.let { rectInner ->
                canvas.drawArc(rectInner, 180F, 180F, useCenter, paintInnerArc)
            }
            canvas.drawPath(needlePath, needleStrokePaint)
            canvas.drawPath(needlePath, needlePaint)
            canvas.drawPath(divisionPath, divisionPaint)
//            canvas.drawTextOnPath(numberString, textPath,0F,0F, textPaint )
            canvas.drawPath(textPath, textPaint)
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


        //add the outer arc. This will move from base towards the top.
        divisionPathForArc1.arcTo(rectOuter, 180F, angle.toFloat())
        //add line towards the inner arc
        divisionPathForArc1.lineTo(
            divisionPointsForArc[1].x.toFloat(), divisionPointsForArc[1].y.toFloat()
        )
        //add line to inner circle's bottom
        divisionPathForArc1.lineTo(width * outerRingThicknessRatio.toFloat(), (width / 2).toFloat())
        //tell path to close
        divisionPathForArc1.close()




        //add arc from lower point towards the top
        divisionPathForArc2.arcTo(rectOuter, 180 + angle.toFloat(), angle.toFloat())
        //add line towards the inner arc
        divisionPathForArc2.lineTo(
            divisionPointsForArc[3].x.toFloat(),
            divisionPointsForArc[3].y.toFloat()
        )
        //add line to the base of arc on the inner circle along the inner circle
        divisionPathForArc2.lineTo(
            divisionPointsForArc[1].x.toFloat(),
            divisionPointsForArc[1].y.toFloat()
        )


        //add arc from lower point towards the top
        divisionPathForArc3.arcTo(rectOuter, 180 + 2 * angle.toFloat(), angle.toFloat())
        //add line towards the inner arc
        divisionPathForArc3.lineTo(
            divisionPointsForArc[5].x.toFloat(),
            divisionPointsForArc[5].y.toFloat()
        )
        //add line to the base of arc on the inner circle along the inner circle
        divisionPathForArc3.lineTo(
            divisionPointsForArc[3].x.toFloat(),
            divisionPointsForArc[3].y.toFloat()
        )


        //add arc from upper point towards the lower point
        divisionPathForArc4.arcTo(rectOuter, 180 + 3 * angle.toFloat(), angle.toFloat())
        //add line towards the inner arc
        divisionPathForArc4.lineTo(
            divisionPointsForArc[7].x.toFloat(),
            divisionPointsForArc[7].y.toFloat()
        )
        //add line to the top of arc on the inner circle along the inner circle
        divisionPathForArc4.lineTo(
            divisionPointsForArc[5].x.toFloat(),
            divisionPointsForArc[5].y.toFloat()
        )


        //add arc from upper point towards the lower point
        divisionPathForArc5.arcTo(rectOuter, 180 + 4 * angle.toFloat(), angle.toFloat())
        //add line towards the inner arc
        divisionPathForArc5.lineTo(
            width * (1 - outerRingThicknessRatio.toFloat()),
            (width / 2).toFloat()
        )
        //add line to the top of arc on the inner circle along the inner circle
        divisionPathForArc5.lineTo(
            divisionPointsForArc[7].x.toFloat(),
            divisionPointsForArc[7].y.toFloat()
        )


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
        textPath.addArc(rectInner , 200F,160F)
        val angle = 180 / noOfDivisions
//        textPaint.letterSpacing = (width * sin( angle* Math.PI / 180)).toFloat()
    }

}

enum class LEVEL_TYPE {
    NUMBERED,
    CUSTOM
}
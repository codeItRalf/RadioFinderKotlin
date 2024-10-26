package app.codeitralf.radiofinder.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.R
import app.codeitralf.radiofinder.services.FFTAudioProcessor
import java.lang.System.arraycopy
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow

@UnstableApi
class FFTBandView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private val FREQUENCY_BAND_LIMITS = arrayOf(
            20, 25, 32, 40, 50, 63, 80, 100, 125, 160, 200, 250, 315, 400, 500, 630,
            800, 1000, 1250, 1600, 2000, 2500, 3150, 4000, 5000, 6300, 8000, 10000,
            12500, 16000, 20000
        )
    }

    private val bands = FREQUENCY_BAND_LIMITS.size
    private val size = FFTAudioProcessor.SAMPLE_SIZE / 2
    private val maxConst = 25_000

    private val fft: FloatArray = FloatArray(size)
    private val paintBandsFill = Paint()
    private val paintBands = Paint()
    private val paintAvg = Paint()
    private val paintPath = Paint()

    private val smoothingFactor = 3
    private val previousValues = FloatArray(bands * smoothingFactor)
    private val fftPath = Path()

    private var startedAt: Long = 0
    private var isActive = false

    init {
        keepScreenOn = true
        setupPaints(context)
    }

    private fun setupPaints(context: Context) {
        paintBandsFill.color = ContextCompat.getColor(context, R.color.white_alpha_50)
        paintBandsFill.style = Paint.Style.FILL

        paintBands.color = ContextCompat.getColor(context, R.color.neon_blue)
        paintBands.strokeWidth = 1f
        paintBands.style = Paint.Style.STROKE

        paintAvg.color = ContextCompat.getColor(context, R.color.neon_pink)
        paintAvg.strokeWidth = 2f
        paintAvg.style = Paint.Style.STROKE

        paintPath.color = ContextCompat.getColor(context, R.color.neon_blue)
        paintPath.strokeWidth = 8f
        paintPath.isAntiAlias = true
        paintPath.style = Paint.Style.STROKE

        setBackgroundColor(ContextCompat.getColor(context, R.color.black))
    }

    fun setColor(fillColor: Int, bandsColor: Int, avgColor: Int, pathColor: Int) {
        paintBandsFill.color = fillColor
        paintBands.color = bandsColor
        paintAvg.color = avgColor
        paintPath.color = pathColor
    }

    fun onFFT(fft: FloatArray) {
        synchronized(this.fft) {
            if (fft.isEmpty()) {
                clearVisualization()
                return
            }

            if (startedAt == 0L) {
                startedAt = System.currentTimeMillis()
            }

            if (fft.size >= size + 2) {
                isActive = true
                arraycopy(fft, 2, this.fft, 0, size)
                postInvalidate()
            }
        }
    }

    private fun clearVisualization() {
        synchronized(this.fft) {
            isActive = false
            fft.fill(0f)
            previousValues.fill(0f)
            startedAt = 0L
            postInvalidate()
        }
    }

    private fun drawAudio(canvas: Canvas) {
        if (!isActive) {
            canvas.drawColor(Color.TRANSPARENT)
            return
        }

        canvas.drawColor(Color.TRANSPARENT)

        var currentFftPosition = 0
        var currentFrequencyBandLimitIndex = 0
        fftPath.reset()
        fftPath.moveTo(0f, height.toFloat())
        var currentAverage = 0f

        while (currentFftPosition < size) {
            var accum = 0f
            val nextLimitAtPosition = floor(FREQUENCY_BAND_LIMITS[currentFrequencyBandLimitIndex] / 20_000.toFloat() * size).toInt()

            synchronized(fft) {
                for (j in 0 until (nextLimitAtPosition - currentFftPosition) step 2) {
                    val raw = (fft[currentFftPosition + j].toDouble().pow(2.0) +
                            fft[currentFftPosition + j + 1].toDouble().pow(2.0)).toFloat()

                    val m = bands / 2
                    val windowed = raw * (0.54f - 0.46f * cos(2 * Math.PI * currentFrequencyBandLimitIndex / (m + 1))).toFloat()
                    accum += windowed
                }
            }
            if (nextLimitAtPosition - currentFftPosition != 0) {
                accum /= (nextLimitAtPosition - currentFftPosition)
            } else {
                accum = 0.0f
            }
            currentFftPosition = nextLimitAtPosition

            var smoothedAccum = accum
            for (i in 0 until smoothingFactor) {
                smoothedAccum += previousValues[i * bands + currentFrequencyBandLimitIndex]
                if (i != smoothingFactor - 1) {
                    previousValues[i * bands + currentFrequencyBandLimitIndex] =
                        previousValues[(i + 1) * bands + currentFrequencyBandLimitIndex]
                } else {
                    previousValues[i * bands + currentFrequencyBandLimitIndex] = accum
                }
            }
            smoothedAccum /= (smoothingFactor + 1)

            currentAverage += smoothedAccum / bands

            val leftX = width * (currentFrequencyBandLimitIndex / bands.toFloat())
            val rightX = leftX + width / bands.toFloat()

            val barHeight = (height * (smoothedAccum / maxConst.toDouble()).coerceAtMost(1.0).toFloat())
            val top = height - barHeight

            canvas.drawRect(leftX, top, rightX, height.toFloat(), paintBandsFill)
            canvas.drawRect(leftX, top, rightX, height.toFloat(), paintBands)

            fftPath.lineTo((leftX + rightX) / 2, top)

            currentFrequencyBandLimitIndex++
        }

        canvas.drawPath(fftPath, paintPath)
        canvas.drawLine(0f, height * (1 - (currentAverage / maxConst)), width.toFloat(), height * (1 - (currentAverage / maxConst)), paintAvg)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawAudio(canvas)
        if (isActive) {
            postInvalidate()
        }
    }
}
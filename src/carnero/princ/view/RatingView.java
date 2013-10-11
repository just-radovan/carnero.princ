package carnero.princ.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import carnero.princ.R;

@SuppressWarnings("unused")
public class RatingView extends View {

	protected Paint mBackground;
	protected Paint mForeground;
	protected float mRating = -1;

	public RatingView(Context context) {
		super(context);
		init(context);
	}

	public RatingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public RatingView(Context context, AttributeSet attrs, int style) {
		super(context, attrs, style);
		init(context);
	}

	@Override
	public void onMeasure(int width, int height) {
		super.onMeasure(width, height);

		setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mRating < 0) {
			mRating = 0;
		}

		float part = (360f / 100f) * mRating;

		RectF oval = new RectF(0, 0, getWidth(), getHeight());

		canvas.save();
		canvas.drawArc(oval, 0, 360, true, mBackground);
		canvas.drawArc(oval, -90 - part, part, true, mForeground);
		canvas.restore();
	}

	protected void init(Context context) {
		mBackground = new Paint();
		mBackground.setColor(context.getResources().getColor(R.color.bg_rating));
		mBackground.setStyle(Paint.Style.FILL_AND_STROKE);
		mBackground.setAntiAlias(true);

		mForeground = new Paint();
		mForeground.setColor(context.getResources().getColor(R.color.fg_rating));
		mForeground.setStyle(Paint.Style.FILL_AND_STROKE);
		mForeground.setAntiAlias(true);
	}

	public void setRating(float rating) {
		mRating = rating;

		invalidate();
	}
}

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.phoenix.imageviewex;

import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * A Drawable that wraps a bitmap and can be tiled, stretched, or aligned. You
 * can create a BitmapDrawable from a file path, an input stream, through XML
 * inflation, or from a {@link android.graphics.Bitmap} object.
 * <p>
 * It can be defined in an XML file with the
 * <code>&lt;bitmap></code> element.  For more
 * information, see the guide to <a
 * href="{@docRoot}guide/topics/resources/drawable-resource.html">Drawable
 * Resources</a>.
 * </p>
 * <p>
 * Also see the {@link android.graphics.Bitmap} class, which handles the
 * management and transformation of raw bitmap graphics, and should be used when
 * drawing to a {@link android.graphics.Canvas}.
 * </p>
 * 
 * @attr ref android.R.styleable#BitmapDrawable_src
 * @attr ref android.R.styleable#BitmapDrawable_antialias
 * @attr ref android.R.styleable#BitmapDrawable_filter
 * @attr ref android.R.styleable#BitmapDrawable_dither
 * @attr ref android.R.styleable#BitmapDrawable_gravity
 * @attr ref android.R.styleable#BitmapDrawable_tileMode
 */
public class CY_BitmapDrawable extends Drawable {

    private static final int DEFAULT_PAINT_FLAGS = Paint.FILTER_BITMAP_FLAG
	    | Paint.DITHER_FLAG;
    private BitmapState mBitmapState;
    private Bitmap mBitmap;
    private int mTargetDensity;

    private final Rect mDstRect = new Rect(); // Gravity.apply() sets this

    private boolean mApplyGravity;
    private boolean mRebuildShader;
    private boolean mMutated;

    // These are scaled to match the target density.
    private int mBitmapWidth;
    private int mBitmapHeight;

    /**
     * Create an empty drawable, not dealing with density.
     * 
     * @deprecated Use {@link #CY_BitmapDrawable(Resources)} to ensure that the
     *             drawable has correctly set its target density.
     */
    @Deprecated
    public CY_BitmapDrawable() {
	mBitmapState = new BitmapState((Bitmap) null);
    }

    /**
     * Create an empty drawable, setting initial target density based on the
     * display metrics of the resources.
     */
    public CY_BitmapDrawable(Resources res) {
	mBitmapState = new BitmapState((Bitmap) null);
	mBitmapState.mTargetDensity = mTargetDensity;
    }

    /**
     * Create drawable from a bitmap, not dealing with density.
     * 
     * @deprecated Use {@link #CY_BitmapDrawable(Resources, Bitmap)} to ensure
     *             that the drawable has correctly set its target density.
     */
    @Deprecated
    public CY_BitmapDrawable(Bitmap bitmap) {
	this(new BitmapState(bitmap), null);
    }

    /**
     * Create drawable from a bitmap, setting initial target density based on
     * the display metrics of the resources.
     */
    public CY_BitmapDrawable(Resources res, Bitmap bitmap) {
	this(new BitmapState(bitmap), res);
	mBitmapState.mTargetDensity = mTargetDensity;
    }

    /**
     * Create a drawable by opening a given file path and decoding the bitmap.
     * 
     * @deprecated Use {@link #CY_BitmapDrawable(Resources, String)} to ensure
     *             that the drawable has correctly set its target density.
     */
    @Deprecated
    public CY_BitmapDrawable(String filepath) {
	this(new BitmapState(BitmapFactory.decodeFile(filepath)), null);
	if (mBitmap == null) {
	    android.util.Log.w("BitmapDrawable",
		    "BitmapDrawable cannot decode " + filepath);
	}
    }

    /**
     * Create a drawable by opening a given file path and decoding the bitmap.
     */
    public CY_BitmapDrawable(Resources res, String filepath) {
	this(new BitmapState(BitmapFactory.decodeFile(filepath)), null);
	mBitmapState.mTargetDensity = mTargetDensity;
	if (mBitmap == null) {
	    android.util.Log.w("BitmapDrawable",
		    "BitmapDrawable cannot decode " + filepath);
	}
    }

    /**
     * Create a drawable by decoding a bitmap from the given input stream.
     * 
     * @deprecated Use
     *             {@link #CY_BitmapDrawable(Resources, java.io.InputStream)} to
     *             ensure that the drawable has correctly set its target
     *             density.
     */
    @Deprecated
    public CY_BitmapDrawable(java.io.InputStream is) {
	this(new BitmapState(BitmapFactory.decodeStream(is)), null);
	if (mBitmap == null) {
	    android.util.Log.w("BitmapDrawable",
		    "BitmapDrawable cannot decode " + is);
	}
    }

    /**
     * Create a drawable by decoding a bitmap from the given input stream.
     */
    public CY_BitmapDrawable(Resources res, java.io.InputStream is) {
	this(new BitmapState(BitmapFactory.decodeStream(is)), null);
	mBitmapState.mTargetDensity = mTargetDensity;
	if (mBitmap == null) {
	    android.util.Log.w("BitmapDrawable",
		    "BitmapDrawable cannot decode " + is);
	}
    }

    public final Paint getPaint() {
	return mBitmapState.mPaint;
    }

    public final Bitmap getBitmap() {
	return mBitmap;
    }

    private void computeBitmapSize() {
	mBitmapWidth = mBitmap.getScaledWidth(mTargetDensity);
	mBitmapHeight = mBitmap.getScaledHeight(mTargetDensity);
    }

    public void setBitmap(Bitmap bitmap) {
	mBitmap = bitmap;
	if (bitmap != null) {
	    computeBitmapSize();
	} else {
	    mBitmapWidth = mBitmapHeight = -1;
	}
    }

    /**
     * Set the density scale at which this drawable will be rendered. This
     * method assumes the drawable will be rendered at the same density as the
     * specified canvas.
     * 
     * @param canvas
     *            The Canvas from which the density scale must be obtained.
     * 
     * @see android.graphics.Bitmap#setDensity(int)
     * @see android.graphics.Bitmap#getDensity()
     */
    public void setTargetDensity(Canvas canvas) {
	setTargetDensity(canvas.getDensity());
    }

    /**
     * Set the density scale at which this drawable will be rendered.
     * 
     * @param metrics
     *            The DisplayMetrics indicating the density scale for this
     *            drawable.
     * 
     * @see android.graphics.Bitmap#setDensity(int)
     * @see android.graphics.Bitmap#getDensity()
     */
    public void setTargetDensity(DisplayMetrics metrics) {
	mTargetDensity = metrics.densityDpi;
	if (mBitmap != null) {
	    computeBitmapSize();
	}
    }

    /**
     * Set the density at which this drawable will be rendered.
     * 
     * @param density
     *            The density scale for this drawable.
     * 
     * @see android.graphics.Bitmap#setDensity(int)
     * @see android.graphics.Bitmap#getDensity()
     */
    public void setTargetDensity(int density) {
	mTargetDensity = density == 0 ? DisplayMetrics.DENSITY_DEFAULT
		: density;
	if (mBitmap != null) {
	    computeBitmapSize();
	}
    }

    /**
     * Get the gravity used to position/stretch the bitmap within its bounds.
     * See android.view.Gravity
     * 
     * @return the gravity applied to the bitmap
     */
    public int getGravity() {
	return mBitmapState.mGravity;
    }

    /**
     * Set the gravity used to position/stretch the bitmap within its bounds.
     * See android.view.Gravity
     * 
     * @param gravity
     *            the gravity
     */
    public void setGravity(int gravity) {
	mBitmapState.mGravity = gravity;
	mApplyGravity = true;
    }

    public void setAntiAlias(boolean aa) {
	mBitmapState.mPaint.setAntiAlias(aa);
    }

    @Override
    public void setFilterBitmap(boolean filter) {
	mBitmapState.mPaint.setFilterBitmap(filter);
    }

    @Override
    public void setDither(boolean dither) {
	mBitmapState.mPaint.setDither(dither);
    }

    public Shader.TileMode getTileModeX() {
	return mBitmapState.mTileModeX;
    }

    public Shader.TileMode getTileModeY() {
	return mBitmapState.mTileModeY;
    }

    public void setTileModeX(Shader.TileMode mode) {
	setTileModeXY(mode, mBitmapState.mTileModeY);
    }

    public final void setTileModeY(Shader.TileMode mode) {
	setTileModeXY(mBitmapState.mTileModeX, mode);
    }

    public void setTileModeXY(Shader.TileMode xmode, Shader.TileMode ymode) {
	final BitmapState state = mBitmapState;
	if (state.mPaint.getShader() == null || state.mTileModeX != xmode
		|| state.mTileModeY != ymode) {
	    state.mTileModeX = xmode;
	    state.mTileModeY = ymode;
	    mRebuildShader = true;
	}
    }

    @Override
    public int getChangingConfigurations() {
	return super.getChangingConfigurations()
		| mBitmapState.mChangingConfigurations;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
	super.onBoundsChange(bounds);
	mApplyGravity = true;
    }

    @Override
    public void draw(Canvas canvas) {
	Bitmap bitmap = mBitmap;
	if (bitmap != null) {
	    final BitmapState state = mBitmapState;
	    if (mRebuildShader) {
		Shader.TileMode tmx = state.mTileModeX;
		Shader.TileMode tmy = state.mTileModeY;

		if (tmx == null && tmy == null) {
		    state.mPaint.setShader(null);
		} else {
		    Shader s = new BitmapShader(bitmap,
			    tmx == null ? Shader.TileMode.CLAMP : tmx,
			    tmy == null ? Shader.TileMode.CLAMP : tmy);
		    state.mPaint.setShader(s);
		}
		mRebuildShader = false;
		copyBounds(mDstRect);
	    }

	    Shader shader = state.mPaint.getShader();
	    if (shader == null) {
		if (mApplyGravity) {
		    Gravity.apply(state.mGravity, mBitmapWidth, mBitmapHeight,
			    getBounds(), mDstRect);
		    mApplyGravity = false;
		}
		canvas.drawBitmap(bitmap, null, mDstRect, state.mPaint);
	    } else {
		if (mApplyGravity) {
		    mDstRect.set(getBounds());
		    mApplyGravity = false;
		}
		canvas.drawRect(mDstRect, state.mPaint);
	    }
	}
    }

    @Override
    public void setAlpha(int alpha) {
	mBitmapState.mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
	mBitmapState.mPaint.setColorFilter(cf);
    }

    /**
     * A mutable BitmapDrawable still shares its Bitmap with any other Drawable
     * that comes from the same resource.
     * 
     * @return This drawable.
     */
    @Override
    public Drawable mutate() {
	if (!mMutated && super.mutate() == this) {
	    mBitmapState = new BitmapState(mBitmapState);
	    mMutated = true;
	}
	return this;
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
	    throws XmlPullParserException, IOException {
	super.inflate(r, parser, attrs);
    }

    @Override
    public int getIntrinsicWidth() {
	return mBitmapWidth;
    }

    @Override
    public int getIntrinsicHeight() {
	return mBitmapHeight;
    }

    @Override
    public int getOpacity() {
	if (mBitmapState.mGravity != Gravity.FILL) {
	    return PixelFormat.TRANSLUCENT;
	}
	Bitmap bm = mBitmap;
	return (bm == null || bm.hasAlpha() || mBitmapState.mPaint.getAlpha() < 255) ? PixelFormat.TRANSLUCENT
		: PixelFormat.OPAQUE;
    }

    @Override
    public final ConstantState getConstantState() {
	mBitmapState.mChangingConfigurations = super
		.getChangingConfigurations();
	return mBitmapState;
    }

    final static class BitmapState extends ConstantState {
	Bitmap mBitmap;
	int mChangingConfigurations;
	int mGravity = Gravity.FILL;
	Paint mPaint = new Paint(DEFAULT_PAINT_FLAGS);
	Shader.TileMode mTileModeX;
	Shader.TileMode mTileModeY;
	int mTargetDensity = DisplayMetrics.DENSITY_DEFAULT;

	BitmapState(Bitmap bitmap) {
	    mBitmap = bitmap;
	}

	BitmapState(BitmapState bitmapState) {
	    this(bitmapState.mBitmap);
	    mChangingConfigurations = bitmapState.mChangingConfigurations;
	    mGravity = bitmapState.mGravity;
	    mTileModeX = bitmapState.mTileModeX;
	    mTileModeY = bitmapState.mTileModeY;
	    mTargetDensity = bitmapState.mTargetDensity;
	    mPaint = new Paint(bitmapState.mPaint);
	}

	@Override
	public Drawable newDrawable() {
	    return new CY_BitmapDrawable(this, null);
	}

	@Override
	public Drawable newDrawable(Resources res) {
	    return new CY_BitmapDrawable(this, res);
	}

	@Override
	public int getChangingConfigurations() {
	    return mChangingConfigurations;
	}
    }

    private CY_BitmapDrawable(BitmapState state, Resources res) {
	mBitmapState = state;
	if (res != null) {
	    mTargetDensity = res.getDisplayMetrics().densityDpi;
	} else if (state != null) {
	    mTargetDensity = state.mTargetDensity;
	} else {
	    mTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
	}
	setBitmap(state.mBitmap);
    }
}
package com.zitano.walter.photography.admin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class PicassoImageGetter implements Html.ImageGetter {
    public  static Context ctx;


    private TextView textView = null;

    public PicassoImageGetter() {

    }

    public PicassoImageGetter(TextView target) {
        textView = target;
    }


    @Override
    public Drawable getDrawable(String source) {
        BitmapDrawablePlaceHolder drawable = new BitmapDrawablePlaceHolder();
        Picasso.get()
                .load(source)
                /*.centerInside()
                .resize(600,200)*/
                .placeholder(R.drawable.ic_loader)
                .into(drawable);
        return drawable;
    }

    private class BitmapDrawablePlaceHolder extends BitmapDrawable implements Target {

        protected Drawable drawable;



        @Override
        public void draw(final Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();

            drawable.setBounds(0, 0, width, height);
            setBounds(0, 0, width, height);


            if (textView != null) {
                textView.setText(textView.getText());
            }
        }

       /* @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            setDrawable(new BitmapDrawable(Context.get().getResources(), bitmap));
        }*/

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            setDrawable(new BitmapDrawable(Walter_Admin.getAppContext().getResources(), bitmap));
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }

    }
}



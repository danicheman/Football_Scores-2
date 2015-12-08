package barqsoft.footballscores.svg;

import android.content.Context;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.caverock.androidsvg.SVG;

import java.io.InputStream;

import barqsoft.footballscores.R;

/**
 * Created by NICK on 12/5/2015.
 *
 * create this object separately for each context.
 */
public class GlideSvgLoader {

    private GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder;
    private int errorImage = 0;
    private int placeHolderImage = R.drawable.ic_launcher;

    public GlideSvgLoader(Context context) {
        requestBuilder = Glide.with(context)
                .using(Glide.buildStreamModelLoader(Uri.class, context), InputStream.class)
                .from(Uri.class)
                .as(SVG.class)
                .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
                .sourceEncoder(new StreamEncoder())
                .cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder()))
                .decoder(new SvgDecoder())
                .placeholder(R.drawable.ic_launcher)
                .error(R.drawable.no_icon)
                .animate(android.R.anim.fade_in)
                .listener(new SvgSoftwareLayerSetter<Uri>())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE);

    }

    public void loadNetSvgIntoImageView(Uri uri, ImageView imageView) {
        requestBuilder.load(uri).into(imageView);
    }
}

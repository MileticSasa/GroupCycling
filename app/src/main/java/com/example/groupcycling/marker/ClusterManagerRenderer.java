package com.example.groupcycling.marker;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.groupcycling.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;

public class ClusterManagerRenderer extends DefaultClusterRenderer<ClusterMarker> {

    private final IconGenerator iconGenerator;
    private ImageView imageView;
    private final int markerWidth, markerHeight;
    private TextView speedTv;

    public ClusterManagerRenderer(Context context, GoogleMap map, ClusterManager<ClusterMarker> clusterManager) {
        super(context, map, clusterManager);

        iconGenerator = new IconGenerator(context);
        imageView = new ImageView(context);
        markerWidth = 75;
        markerHeight = 75;
        imageView.setLayoutParams(new ViewGroup.LayoutParams(markerWidth, markerHeight));
        imageView.setPadding(2, 2, 2, 2);
        iconGenerator.setContentView(imageView);
        speedTv = new TextView(context);
        speedTv.setTextSize(25);
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull ClusterMarker item, @NonNull MarkerOptions markerOptions) {

        String profileImage = item.getImage();

        try{
            Picasso.get().load(profileImage).fit().into(imageView);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.ic_image_default).into(imageView);
        }

        Bitmap icon = iconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.getTitle());
    }

    @Override
    protected boolean shouldRenderAsCluster(@NonNull Cluster<ClusterMarker> cluster) {
        return false;
    }

    public void setUpdatedMarker(ClusterMarker clusterMarker){
        Marker marker = getMarker(clusterMarker);
        if(marker != null){
            marker.setPosition(clusterMarker.getPosition());
        }
    }
}

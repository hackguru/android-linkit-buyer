package ams.android.linkitmerchant.Fragment;

import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import ams.android.linkitmerchant.R;

public class FragmentIntro extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!(getActivity().getRequestedOrientation() ==ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        View rootView = inflater.inflate(R.layout.fragment_intro, container, false);
        VideoView videoView = (VideoView) rootView.findViewById(R.id.videoViewIntro);
        String UrlPath = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.introvideo;
        videoView.setVideoURI(Uri.parse(UrlPath));
        videoView.start();
        return rootView;
    }
}

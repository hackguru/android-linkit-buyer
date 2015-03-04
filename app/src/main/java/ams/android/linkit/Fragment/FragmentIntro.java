package ams.android.linkit.Fragment;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import ams.android.linkit.Activity.MainActivity;
import ams.android.linkit.R;

public class FragmentIntro extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity) getActivity()).currentFragmentName = "Intro";
        View rootView = inflater.inflate(R.layout.fragment_intro, container, false);
        VideoView videoView = (VideoView) rootView.findViewById(R.id.videoViewIntro);
        String UrlPath = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.introvideo_shopper_square;
        videoView.setVideoURI(Uri.parse(UrlPath));
        videoView.start();
        return rootView;
    }
}

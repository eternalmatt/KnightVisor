package com.visor.knight.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.visor.knight.R;

public class ControlsFragment extends Fragment {

	private SeekBar seekBar;
	private ActivityDotH activity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = (ActivityDotH) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.controls, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setMax(150);
        seekBar.setProgress(75);
		seekBar.setOnSeekBarChangeListener(activity.getOnSeekBarChangeListener());
	}
}

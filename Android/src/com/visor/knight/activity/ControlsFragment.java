package com.visor.knight.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;

import com.visor.knight.R;

public class ControlsFragment extends Fragment {

	private SeekBar seekBar;
	private EditText kernel[] = new EditText[9];
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
		seekBar.setOnSeekBarChangeListener(activity.getOnSeekBarChangeListener());
		kernel = getKernelFromView(view);
		for(EditText cell : kernel){
			cell.addTextChangedListener(onKernelChangeListener);
		}
	}

	private static EditText[] getKernelFromView(final View view) {
		EditText kernel[] = new EditText[9];
		kernel[0] = (EditText) view.findViewById(R.id.kernel_cell_11);
		kernel[1] = (EditText) view.findViewById(R.id.kernel_cell_12);
		kernel[2] = (EditText) view.findViewById(R.id.kernel_cell_13);
		kernel[3] = (EditText) view.findViewById(R.id.kernel_cell_21);
		kernel[4] = (EditText) view.findViewById(R.id.kernel_cell_22);
		kernel[5] = (EditText) view.findViewById(R.id.kernel_cell_23);
		kernel[6] = (EditText) view.findViewById(R.id.kernel_cell_31);
		kernel[7] = (EditText) view.findViewById(R.id.kernel_cell_32);
		kernel[8] = (EditText) view.findViewById(R.id.kernel_cell_33);
		return null;
	}
	
    private final TextWatcher onKernelChangeListener = new TextWatcher() {
		@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		@Override public void afterTextChanged(final Editable s) {
			int nums[][] = new int[3][3];
			for(int i=0; i < 3; i++){
				for(int j=0; j < 3; j++){
					String text = kernel[i*3+j].getText().toString();
					int x = Integer.getInteger(text, 0);
					nums[i][j] = x;
				}
			}
			activity.setKernel(nums);
		}
	};

}

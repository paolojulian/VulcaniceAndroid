package com.vulcanice.vulcanice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.vulcanice.vulcanice.R;

/**
 * Created by paolo on 5/3/18.
 */

public class DashBoardTab3 extends Fragment {
    private static final String TAG = "Tab3Fragment";
    private Button btnTab3;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dash_board_3, container, false);
        btnTab3 = view.findViewById(R.id.btnTab3);

        btnTab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "TEST TAB1", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}

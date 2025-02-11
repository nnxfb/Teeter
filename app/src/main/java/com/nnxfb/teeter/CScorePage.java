package com.nnxfb.teeter;

import android.app.Activity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

/* loaded from: classes3.dex */
public class CScorePage {
    private Activity mActivity;
    private TableLayout mLayout;
    private TextView mView0;
    private TextView mView1;
    private TextView mView2;
    private TextView mView3;
    private TextView mView4;

    public CScorePage(Activity activity) {
        this.mActivity = activity;
    }

    public View fnCreateView() {
        return fnLayoutScores(CU.LEVEL);
    }

    public void fnInvalidate() {
        this.mLayout.invalidate();
    }

    private View fnLayoutScores(int level) {
        if (this.mLayout == null) {
            this.mLayout = (TableLayout) View.inflate(this.mActivity, R.layout.layout01, null);
        }
        if (this.mView0 == null) {
            this.mView0 = (TextView) this.mLayout.findViewById(R.id.level_caption);
        }
        String str = String.format(this.mActivity.getResources().getString(R.string.str_level_caption), Integer.valueOf(level));
        this.mView0.setText(str);
        if (this.mView1 == null) {
            this.mView1 = (TextView) this.mLayout.findViewById(R.id.level_time_score);
        }
        CTime t = new CTime(CS.sfnGetLevelTime());
        String str2 = String.format("%02d:%02d:%02d", Integer.valueOf(t.fnGetHours()), Integer.valueOf(t.fnGetMinutes()), Integer.valueOf(t.fnGetSeconds()));
        this.mView1.setText(str2);
        if (this.mView2 == null) {
            this.mView2 = (TextView) this.mLayout.findViewById(R.id.level_attempt_score);
        }
        String str3 = String.valueOf(CS.sfnGetLevelAttempt());
        this.mView2.setText(str3);
        if (this.mView3 == null) {
            this.mView3 = (TextView) this.mLayout.findViewById(R.id.total_time_score);
        }
        CTime t2 = new CTime(CS.sfnGetTotalTime());
        String str4 = String.format("%02d:%02d:%02d", Integer.valueOf(t2.fnGetHours()), Integer.valueOf(t2.fnGetMinutes()), Integer.valueOf(t2.fnGetSeconds()));
        this.mView3.setText(str4);
        if (this.mView4 == null) {
            this.mView4 = (TextView) this.mLayout.findViewById(R.id.total_attempt_score);
        }
        String str5 = String.valueOf(CS.sfnGetTotalAttempt());
        this.mView4.setText(str5);
        return this.mLayout;
    }

    public void clearMemory() {
        this.mActivity = null;
        this.mLayout = null;
        this.mView0 = null;
        this.mView1 = null;
        this.mView2 = null;
        this.mView3 = null;
        this.mView4 = null;
    }
}

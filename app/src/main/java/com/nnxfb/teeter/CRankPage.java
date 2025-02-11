package com.nnxfb.teeter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import androidx.core.view.ViewCompat;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.Arrays;

/* loaded from: classes3.dex */
public class CRankPage {
    private static final String RANK_FILENAME = "rank.list";
    private static final int RANK_MAX_COUNT = 5;
    private Activity mActivity;

    public CRankPage(Activity activity) {
        this.mActivity = activity;
    }

    public View fnCreateView() {
        int length;
        long[] newRankArray;
        long[] storedRankArray;
        TableLayout layout = (TableLayout) View.inflate(this.mActivity, R.layout.layout02, null);
        long[] rankArray = fnGetStoredRanks();
        if (rankArray != null && rankArray.length != 0) {
            length = rankArray.length + 1;
            newRankArray = new long[length];
            System.arraycopy(rankArray, 0, newRankArray, 0, length - 1);
        } else {
            length = 1;
            newRankArray = new long[1];
        }
        long newRank = CS.sfnGetTotalTime();
        newRankArray[length - 1] = newRank;
        Arrays.sort(newRankArray);
        int nHighlight = -1;
        int len = Math.min(5, length);
        for (int i = 0; i < len; i++) {
            CTime t = new CTime(newRankArray[i]);
            @SuppressLint("DefaultLocale")
            String str = String.format("%02d:%02d:%02d", t.fnGetHours(), t.fnGetMinutes(), t.fnGetSeconds());
            ((TextView) layout.findViewById(R.id.rank_scores1 + i)).setText(str);
            if (newRank == newRankArray[i]) {
                nHighlight = i;
            }
        }
        if (nHighlight != -1) {
            TextView rankView = layout.findViewById(R.id.rank_scores1 + nHighlight);
            rankView.setTextColor(ColorStateList.valueOf(ViewCompat.MEASURED_STATE_MASK));
            rankView.setBackgroundColor(-1);
        }
        if (len != length) {
            storedRankArray = new long[len];
            System.arraycopy(newRankArray, 0, storedRankArray, 0, len);
        } else {
            storedRankArray = newRankArray;
        }
        fnStoreRanks(storedRankArray);
        Button newGameBtn = layout.findViewById(R.id.btn_restart);
        newGameBtn.setOnClickListener(v -> {
            if (CRankPage.this.mActivity instanceof CTeeterActivity) {
                CTeeterActivity teeterAct = (CTeeterActivity) CRankPage.this.mActivity;
                teeterAct.fnExternalGameFlow(2);
            }
        });
        Button leaveBtn = layout.findViewById(R.id.btn_quit);
        leaveBtn.setOnClickListener(v -> {
            if (CRankPage.this.mActivity instanceof CTeeterActivity) {
                CTeeterActivity teeterAct = (CTeeterActivity) CRankPage.this.mActivity;
                teeterAct.fnExternalGameFlow(3);
            }
        });
        return layout;
    }

    private boolean fnStoreRanks(long[] rankArray) {
        int length;
        if (rankArray == null || (length = rankArray.length) <= 0) {
            return false;
        }
        try {
            FileOutputStream fos = this.mActivity.openFileOutput(RANK_FILENAME, Context.MODE_PRIVATE);
            DataOutputStream dos2 = new DataOutputStream(fos);
            try {
                dos2.writeInt(length);
                for (long j : rankArray) {
                    dos2.writeLong(j);
                }
                closeResource(dos2);
                closeResource(fos);
            } catch (FileNotFoundException e3) {
                e3.printStackTrace();
                closeResource(dos2);
                closeResource(fos);
                return false;
            } catch (IOException e4) {
                e4.printStackTrace();
                closeResource(dos2);
                closeResource(fos);
                return false;
            } catch (Throwable th1) {
                closeResource(dos2);
                closeResource(fos);
                throw th1;
            }
        } catch (IOException ignored) {
        } catch (Throwable th) {
            return true;
        }
        return true;
    }

    private long[] fnGetStoredRanks() {
        FileInputStream fis = null;
        DataInputStream dis2 = null;
        long[] rankArray = null;
        try {
            fis = this.mActivity.openFileInput(RANK_FILENAME);
            dis2 = new DataInputStream(fis);
        } catch (IOException e) {
            Log.e("FileIO", "e:"+e);
            return null;
        }
        try {
            int length = dis2.readInt();
            if (length > 0) {
                rankArray = new long[length];
                for (int i = 0; i < length; i++) {
                    rankArray[i] = dis2.readLong();
                }
            }
            closeResource(dis2);
            closeResource(fis);
            return rankArray;
        } catch (FileNotFoundException e4) {
            e4.printStackTrace();
            closeResource(dis2);
            closeResource(fis);
            return rankArray;
        } catch (StreamCorruptedException e5) {
            e5.printStackTrace();
            closeResource(dis2);
            closeResource(fis);
            return rankArray;
        } catch (IOException e6) {
            e6.printStackTrace();
            closeResource(dis2);
            closeResource(fis);
            return rankArray;
        } catch (Throwable th1) {
            closeResource(dis2);
            closeResource(fis);
            throw th1;
        }
    }

    private void closeResource(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void clearMemory() {
        this.mActivity = null;
    }
}

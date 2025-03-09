package com.example.buoi072;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import java.io.IOException;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
// import androidx.activity.EdgeToEdge; // Bình luận nếu chưa có thư viện này

import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS_CODE = 123;

    private MediaRecorder myRecorder;
    private MediaPlayer myPlayer;
    private String outputFile = null;

    private Button start;
    private Button stop;
    private Button play;
    private Button stopPlay;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nếu chưa có thư viện EdgeToEdge, bạn có thể bình luận dòng này:
        // EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        text = findViewById(R.id.text1);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        play = findViewById(R.id.play);
        stopPlay = findViewById(R.id.stopPlay);

        // Kiểm tra và xin quyền runtime (nếu cần)
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            // Khi đã có đủ quyền, khởi tạo recorder
            initRecorder();
        }

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playRecording();
            }
        });

        stopPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlaying();
            }
        });
    }

    /**
     * Hàm khởi tạo recorder, xác định file ghi âm.
     */
    private void initRecorder() {
        // Sử dụng thư mục riêng của ứng dụng (Music) trên bộ nhớ ngoài:
        File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (externalFilesDir != null) {
            outputFile = externalFilesDir.getAbsolutePath() + "/RecordingName.3gpp";
        } else {
            // Nếu không lấy được (rất hiếm), fallback về bộ nhớ trong
            outputFile = getFilesDir().getAbsolutePath() + "/RecordingName.3gpp";
        }

        // Khởi tạo MediaRecorder
        myRecorder = new MediaRecorder();
        myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        myRecorder.setOutputFile(outputFile);
    }

    /**
     * Bắt đầu ghi âm
     */
    private void startRecording() {
        try {
            myRecorder.prepare();
            myRecorder.start();
            text.setText("Bắt đầu ghi âm...");
            start.setEnabled(false);
            stop.setEnabled(true);
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Dừng ghi âm
     */
    private void stopRecording() {
        try {
            myRecorder.stop();
            myRecorder.release();
            myRecorder = null;

            stop.setEnabled(false);
            play.setEnabled(true);
            text.setText("Dừng ghi âm.");
        } finally {

        }
        try {
            // ...
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Phát lại file ghi âm
     */
    private void playRecording() {
        try {
            myPlayer = new MediaPlayer();
            myPlayer.setDataSource(outputFile);
            myPlayer.prepare();
            myPlayer.start();

            play.setEnabled(false);
            stopPlay.setEnabled(true);
            text.setText("Đang nghe...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Dừng phát lại
     */
    private void stopPlaying() {
        if (myPlayer != null) {
            myPlayer.stop();
            myPlayer.release();
            myPlayer = null;

            play.setEnabled(true);
            stopPlay.setEnabled(false);
            text.setText("Dừng nghe.");
        }
    }

    /**
     * Kiểm tra xem app đã có quyền hay chưa
     */
    private boolean checkPermissions() {
        // Nếu bạn không cần ghi ngoài (chỉ ghi trong thư mục riêng), có thể bỏ WRITE_EXTERNAL_STORAGE
        int recordAudioPermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO);
        int writeStoragePermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return (recordAudioPermission == PackageManager.PERMISSION_GRANTED
                && writeStoragePermission == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Yêu cầu quyền runtime
     */
    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                REQUEST_PERMISSIONS_CODE
        );
    }

    /**
     * Xử lý kết quả khi người dùng cấp hoặc từ chối quyền
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            // Kiểm tra nếu tất cả quyền được cấp
            if (grantResults.length > 0) {
                boolean allGranted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    // Đã được cấp quyền
                    initRecorder();
                } else {
                    // Người dùng từ chối quyền
                    text.setText("Quyền bị từ chối. Không thể ghi âm!");
                }
            }
        }
    }
}

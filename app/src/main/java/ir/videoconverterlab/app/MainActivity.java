package ir.videoconverterlab.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String extStore = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

        long executionId = FFmpeg.executeAsync("-i file1.mp4 -c:v mpeg4 file2.mp4", new ExecuteCallback() {

            @Override
            public void apply(final long executionId, final int rc) {
                if (rc == RETURN_CODE_SUCCESS) {
                    System.out.println("Async command execution completed successfully.");
                } else if (rc == RETURN_CODE_CANCEL) {
                    System.out.println("Async command execution cancelled by user.");
                } else {
                    System.out.println(String.format("Async command execution failed with rc=%d.", rc));
                }
            }
        });
    }
}

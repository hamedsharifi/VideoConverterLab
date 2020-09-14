package ir.videoconverterlab.app;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.CodeBoy.MediaFacer.MediaFacer;
import com.CodeBoy.MediaFacer.VideoGet;
import com.CodeBoy.MediaFacer.mediaHolders.videoContent;
import com.CodeBoy.MediaFacer.mediaHolders.videoFolderContent;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class MainActivity extends AppCompatActivity {

    ArrayList<videoContent> videoContents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    videoContents.addAll(MediaFacer
                            .withVideoContex(MainActivity.this)
                            .getAllVideoContent(VideoGet.externalContentUri));
                    System.out.println(videoContents.size());
//        videoContents.addAll(MediaFacer.withVideoContex(this).getVideoFolders(VideoGet.externalContentUri));

                    try {
                        long executionId = FFmpeg.executeAsync("-y -i " + videoContents.get(7).getPath() + " " + getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/my-video.mp4", new ExecuteCallback() {

                            @Override
                            public void apply(final long executionId, final int rc) {
                                if (rc == RETURN_CODE_SUCCESS) {
                                    saveToMediasStore(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/my-video.mp4");
                                    System.out.println("Async command execution completed successfully.");
                                } else if (rc == RETURN_CODE_CANCEL) {
                                    System.out.println("Async command execution cancelled by user.");
                                } else {
                                    System.out.println(String.format("Async command execution failed with rc=%d.", rc));
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

            }
        }).check();


//        String extStore = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();


    }

    public String safUriToFFmpegPath(final Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
            return String.format(Locale.getDefault(), "pipe:%d", parcelFileDescriptor.getFd());
        } catch (FileNotFoundException e) {
            return "";
        }
    }

    private void saveToMediasStore(String file) {
        String videoFileName = "video_" + System.currentTimeMillis() + ".mp4";

        ContentValues valuesvideos;
        valuesvideos = new ContentValues();
         valuesvideos.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" + "Folder");
        valuesvideos.put(MediaStore.Video.Media.TITLE, videoFileName);
        valuesvideos.put(MediaStore.Video.Media.DISPLAY_NAME, videoFileName);
        valuesvideos.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        valuesvideos.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        valuesvideos.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
        valuesvideos.put(MediaStore.Video.Media.IS_PENDING, 1);
        ContentResolver resolver = this.getContentResolver();
        Uri collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri uriSavedVideo = resolver.insert(collection, valuesvideos);


        ParcelFileDescriptor pfd;

        try {
            pfd = this.getContentResolver().openFileDescriptor(uriSavedVideo, "w");

            FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());

// get the already saved video as fileinputstream
//            File storageDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "Folder");
            File imageFile = new File(file);

            FileInputStream in = new FileInputStream(imageFile);


            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {

                out.write(buf, 0, len);
            }


            out.close();
            in.close();
            pfd.close();




        } catch (Exception e) {

            e.printStackTrace();
        }


        valuesvideos.clear();
        valuesvideos.put(MediaStore.Video.Media.IS_PENDING, 0);
        this.getContentResolver().update(uriSavedVideo, valuesvideos, null, null);
    }
}

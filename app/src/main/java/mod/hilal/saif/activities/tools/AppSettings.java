package mod.hilal.saif.activities.tools;

import static com.besome.sketch.editor.view.ViewEditor.shakeView;

import static pro.sketchware.utility.SketchwareUtil.dpToPx;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;

import a.a.a.aB;

import com.besome.sketch.editor.manage.library.LibraryItemView;
import com.besome.sketch.help.SystemSettingActivity;
import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dev.aldi.sayuti.editor.manage.ManageLocalLibraryActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kellinwood.security.zipsigner.ZipSigner;

import mod.alucard.tn.apksigner.ApkSigner;
import mod.hey.studios.code.SrcCodeEditorLegacy;
import mod.hey.studios.util.Helper;
import mod.khaled.logcat.LogReaderActivity;

import pro.sketchware.R;
import pro.sketchware.activities.editor.component.ManageCustomComponentActivity;
import pro.sketchware.activities.settings.SettingsActivity;
import pro.sketchware.databinding.DialogSelectApkToSignBinding;
import pro.sketchware.utility.SketchwareUtil;
import pro.sketchware.utility.FileUtil;
import pro.sketchware.utility.UI;

public class AppSettings extends BaseAppCompatActivity {

    private LinearLayout content;
    private MaterialToolbar topAppBar;
    private final List<LibraryItemView> preferences = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefences_content_appbar);

        content = findViewById(R.id.content);
        topAppBar = findViewById(R.id.topAppBar);

        topAppBar.setTitle("Settings");
        topAppBar.setNavigationOnClickListener(view -> onBackPressed());
        setupPreferences();
    }

    private void openWorkingDirectory() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.MULTI_MODE;
        properties.selection_type = DialogConfigs.FILE_AND_DIR_SELECT;
        properties.root = getFilesDir().getParentFile();
        properties.error_dir = getExternalCacheDir();
        properties.extensions = null;
        FilePickerDialog dialog = new FilePickerDialog(this, properties, R.style.RoundedCornersDialog);
        dialog.setTitle("Select an entry to modify");
        dialog.setDialogSelectionListener(files -> {
            final boolean isDirectory = new File(files[0]).isDirectory();
            if (files.length > 1 || isDirectory) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Select an action")
                        .setSingleChoiceItems(new String[]{"Delete"}, -1, (actionDialog, which) -> {
                            new MaterialAlertDialogBuilder(this)
                                    .setTitle("Delete " + (isDirectory ? "folder" : "file") + "?")
                                    .setMessage("Are you sure you want to delete this " + (isDirectory ? "folder" : "file") + " permanently? This cannot be undone.")
                                    .setPositiveButton(R.string.common_word_delete, (deleteConfirmationDialog, pressedButton) -> {
                                        for (String file : files) {
                                            FileUtil.deleteFile(file);
                                            deleteConfirmationDialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton(R.string.common_word_cancel, null)
                                    .show();
                            actionDialog.dismiss();
                        })
                        .show();
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Select an action")
                        .setSingleChoiceItems(new String[]{"Edit", "Delete"}, -1, (actionDialog, which) -> {
                            switch (which) {
                                case 0 -> {
                                    Intent intent = new Intent(getApplicationContext(), ConfigActivity.isLegacyCeEnabled() ?
                                            SrcCodeEditorLegacy.class
                                            : mod.hey.studios.code.SrcCodeEditor.class);
                                    intent.putExtra("title", Uri.parse(files[0]).getLastPathSegment());
                                    intent.putExtra("content", files[0]);
                                    intent.putExtra("xml", "");
                                    startActivity(intent);
                                }
                                case 1 -> new MaterialAlertDialogBuilder(this)
                                        .setTitle("Delete file?")
                                        .setMessage("Are you sure you want to delete this file permanently? This cannot be undone.")
                                        .setPositiveButton(R.string.common_word_delete, (deleteDialog, pressedButton) ->
                                                FileUtil.deleteFile(files[0]))
                                        .setNegativeButton(R.string.common_word_cancel, null)
                                        .show();
                            }
                            actionDialog.dismiss();
                        })
                        .show();
            }
        });
        dialog.show();
    }

    private void setupPreferences() {
        preferences.add(createPreference(R.drawable.ic_mtrl_block, "Block manager", "Manage your own blocks to use in Logic Editor", new ActivityLauncher(new Intent(getApplicationContext(), BlocksManager.class)), false));
        preferences.add(createPreference(R.drawable.ic_mtrl_pull_down, "Block selector menu manager", "Manage your own block selector menus", openSettingsActivity(SettingsActivity.BLOCK_SELECTOR_MANAGER_FRAGMENT), false));
        preferences.add(createPreference(R.drawable.ic_mtrl_component, "Component manager", "Manage your own components", new ActivityLauncher(new Intent(getApplicationContext(), ManageCustomComponentActivity.class)), false));
        preferences.add(createPreference(R.drawable.ic_mtrl_list, "Event manager", "Manage your own events", openSettingsActivity(SettingsActivity.EVENTS_MANAGER_FRAGMENT), false));
        preferences.add(createPreference(R.drawable.ic_mtrl_box, "Local library manager", "Manage and download local libraries", new ActivityLauncher(new Intent(getApplicationContext(), ManageLocalLibraryActivity.class), new Pair<>("sc_id", "system")), false));
        preferences.add(createPreference(R.drawable.ic_mtrl_settings_applications, "Mod settings", "Change general mod settings", new ActivityLauncher(new Intent(getApplicationContext(), ConfigActivity.class)), false));
        preferences.add(createPreference(R.drawable.ic_mtrl_palette, getString(R.string.settings_appearance), getString(R.string.settings_appearance_description), openSettingsActivity(SettingsActivity.SETTINGS_APPEARANCE_FRAGMENT), false));
        preferences.add(createPreference(R.drawable.ic_mtrl_folder, "Open working directory", "Open Sketchware Pro's directory and edit files in it", v -> openWorkingDirectory(), false));
        preferences.add(createPreference(R.drawable.ic_mtrl_apk_document, "Sign an APK file with testkey", "Sign an already existing APK file with testkey and signature schemes up to V4", v -> signApkFileDialog(), false));
        preferences.add(createPreference(R.drawable.ic_mtrl_article, getString(R.string.design_drawer_menu_title_logcat_reader), getString(R.string.design_drawer_menu_subtitle_logcat_reader), new ActivityLauncher(new Intent(getApplicationContext(), LogReaderActivity.class)), false));
        preferences.add(createPreference(R.drawable.ic_mtrl_settings, getString(R.string.main_drawer_title_system_settings), "Auto-save and vibrations", new ActivityLauncher(new Intent(getApplicationContext(), SystemSettingActivity.class)), true));
        preferences.forEach(preference -> {
            final int index = preferences.indexOf(preference);
            preference.container.setBackgroundResource(UI.getShapedBackgroundForList(preferences, index));
            content.addView(preference);
        });
    }
    
    private View.OnClickListener openSettingsActivity(String fragmentTag) {
        return v -> {
            Intent intent = new Intent(v.getContext(), SettingsActivity.class);
            intent.putExtra("fragment_tag", fragmentTag);
            v.getContext().startActivity(intent);
        };
    }

    private LibraryItemView createPreference(int icon, String title, String desc, View.OnClickListener listener, boolean lastpreference) {
        LibraryItemView preference = new LibraryItemView(this);
        preference.enabled.setVisibility(View.GONE);
        preference.icon.setImageResource(icon);
        preference.title.setText(title);
        preference.description.setText(desc);
        preference.setOnClickListener(listener);
        LinearLayout.LayoutParams preferenceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.0f
        );
        preferenceParams.bottomMargin = lastpreference ? dpToPx(25) : dpToPx(0);
        preference.setLayoutParams(preferenceParams);
        return preference;
    }

    private void signApkFileDialog() {
        final boolean[] isAPKSelected = {false};
        aB apkPathDialog = new aB(this);
        apkPathDialog.b("Sign APK with testkey");

        DialogSelectApkToSignBinding binding = DialogSelectApkToSignBinding.inflate(getLayoutInflater());
        View testkey_root = binding.getRoot();
        TextView apk_path_txt = binding.apkPathTxt;

        binding.selectFile.setOnClickListener(v -> {
            DialogProperties properties = new DialogProperties();
            properties.selection_mode = DialogConfigs.SINGLE_MODE;
            properties.selection_type = DialogConfigs.FILE_SELECT;
            properties.extensions = new String[]{"apk"};
            FilePickerDialog dialog = new FilePickerDialog(this, properties, R.style.RoundedCornersDialog);
            dialog.setDialogSelectionListener(files -> {
                isAPKSelected[0] = true;
                apk_path_txt.setText(files[0]);
            });
            dialog.show();
        });

        apkPathDialog.b("Continue", v -> {
            if (!isAPKSelected[0]) {
                SketchwareUtil.toast("Please select an APK file to sign", Toast.LENGTH_SHORT);
                shakeView(binding.selectFile);
                return;
            }
            String input_apk_path = apk_path_txt.getText().toString();
            String output_apk_file_name = Uri.fromFile(new File(input_apk_path)).getLastPathSegment();
            String output_apk_path = new File(Environment.getExternalStorageDirectory(),
                    "sketchware/signed_apk/" + output_apk_file_name).getAbsolutePath();

            if (new File(output_apk_path).exists()) {
                aB confirmOverwrite = new aB(this);
                confirmOverwrite.a(R.drawable.color_save_as_new_96);
                confirmOverwrite.b("File exists");
                confirmOverwrite.a("An APK named " + output_apk_file_name + " already exists at /sketchware/signed_apk/.  Overwrite it?");

                confirmOverwrite.a(Helper.getResString(R.string.common_word_cancel), Helper.getDialogDismissListener(confirmOverwrite));
                confirmOverwrite.b("Overwrite", view -> {
                    confirmOverwrite.dismiss();
                    signApkFileWithDialog(input_apk_path, output_apk_path, true,
                            null, null, null, null);
                });
                confirmOverwrite.show();
            } else {
                signApkFileWithDialog(input_apk_path, output_apk_path, true,
                        null, null, null, null);
            }
        });

        apkPathDialog.a(Helper.getResString(R.string.common_word_cancel), v -> apkPathDialog.dismiss());

        apkPathDialog.a(testkey_root);
        apkPathDialog.setCancelable(false);
        apkPathDialog.show();
    }

    private void signApkFileWithDialog(String inputApkPath, String outputApkPath, boolean useTestkey, String keyStorePath, String keyStorePassword, String keyStoreKeyAlias, String keyPassword) {
        View building_root = getLayoutInflater().inflate(R.layout.build_progress_msg_box, null, false);
        LinearLayout layout_quiz = building_root.findViewById(R.id.layout_quiz);
        TextView tv_progress = building_root.findViewById(R.id.tv_progress);

        ScrollView scroll_view = new ScrollView(this);
        TextView tv_log = new TextView(this);
        scroll_view.addView(tv_log);
        layout_quiz.addView(scroll_view);

        tv_progress.setText("Signing APK...");

        AlertDialog building_dialog = new MaterialAlertDialogBuilder(this)
                .setView(building_root)
                .create();

        ApkSigner signer = new ApkSigner();
        new Thread() {
            @Override
            public void run() {
                super.run();

                ApkSigner.LogCallback callback = line -> runOnUiThread(() ->
                        tv_log.setText(tv_log.getText().toString() + line));

                if (useTestkey) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        signer.signWithTestKey(inputApkPath, outputApkPath, callback);
                    } else {
                        try {
                            ZipSigner zipSigner = new ZipSigner();
                            zipSigner.setKeymode(ZipSigner.KEY_TESTKEY);
                            zipSigner.signZip(inputApkPath, outputApkPath);
                        } catch (Exception e) {
                            tv_progress.setText("An error occurred. Check the log for more details.");
                            tv_log.setText("Failed to sign APK with zipsigner: " + e);
                        }
                    }
                } else {
                    signer.signWithKeyStore(inputApkPath, outputApkPath,
                            keyStorePath, keyStorePassword, keyStoreKeyAlias, keyPassword, callback);
                }

                runOnUiThread(() -> {
                    if (callback.errorCount.get() == 0) {
                        building_dialog.dismiss();
                        SketchwareUtil.toast("Successfully saved signed APK to: /Internal storage/sketchware/signed_apk/"
                                        + Uri.fromFile(new File(outputApkPath)).getLastPathSegment(),
                                Toast.LENGTH_LONG);
                    } else {
                        tv_progress.setText("An error occurred. Check the log for more details.");
                    }
                });
            }
        }.start();

        building_dialog.show();
    }

    private class ActivityLauncher implements View.OnClickListener {
        private final Intent launchIntent;
        private Pair<String, String> optionalExtra;

        ActivityLauncher(Intent launchIntent) {
            this.launchIntent = launchIntent;
        }

        ActivityLauncher(Intent launchIntent, Pair<String, String> optionalExtra) {
            this(launchIntent);
            this.optionalExtra = optionalExtra;
        }

        @Override
        public void onClick(View v) {
            if (optionalExtra != null) {
                launchIntent.putExtra(optionalExtra.first, optionalExtra.second);
            }
            startActivity(launchIntent);
        }
    }
}

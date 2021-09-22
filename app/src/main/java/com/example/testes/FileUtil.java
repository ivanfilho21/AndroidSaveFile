package com.example.testes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.testes.callback.FileDestinationCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Ivan Nascimento
 * @since 2021-09-18
 */

public class FileUtil {
    private static final String LOG_TAG = "FileUtil";

    private FileUtil() {}

    @NonNull
    public static ActivityResultLauncher<Intent> startForResult(@NonNull AppCompatActivity activity, @NonNull FileDestinationCallback callback) {
        return activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Uri destinationUri = (result.getData() != null) ? result.getData().getData() : null;
                if (destinationUri != null) {
                    callback.onSuccess(destinationUri);
                    return;
                }
            }
            callback.onError();
        });
    }

    /**
     * Cria um arquivo no diretório especificado.
     * @param directory o nome do diretório no qual o arquivo deve ser salvo.
     * @param fileName nome do arquivo já com a extensão desejada. Exemplo: my_file.png
     * @return o arquivo salvo ou nulo caso o arquivo não exista e não seja possível criá-lo.
     */
    @Nullable
    public static File createFile(@NonNull String directory, @NonNull String fileName) {
        File file = new File(directory, fileName);

        if (file.exists()) return file;

        try {
            return file.createNewFile() ? file : null;
        } catch (IOException e) {
            Log.e(LOG_TAG, null, e);
        }

        return file;
    }

    /**
     * Compartilha um arquivo.
     */
    public static void shareFile(@NonNull Context context, @NonNull File file, @NonNull String mimeType) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .putExtra(Intent.EXTRA_STREAM, uri)
                .putExtra(Intent.EXTRA_TEXT, context.getText(R.string.default_share_text))
                .setType(mimeType);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.default_share_title)));
    }

    /**
     * Salva um arquivo de base64 no cache do App e logo depois abre a ação de compartilhamento.
     *
     * @param fileName nome do arquivo. Caso não possua extensão, será utilizado o campo @mimeType para deduzir a extensão.
     * @param mimeType tipo do arquivo. Exemplo: "application/pdf"
     */
    public static void shareBase64File(@NonNull Context context, @NonNull String base64, @NonNull String fileName, @NonNull String mimeType) {
        String extension = getExtensionFromMimeType(mimeType);
        fileName = fileName + (fileNameHasExtension(fileName) ? "" : extension);
        fileName = filterFileName(fileName);
        File file = createFile(context.getCacheDir().getAbsolutePath(), fileName);

        if (file == null) {
            return;
        }

        byte[] fileAsBytes = decodeBase64(base64);

        try (FileOutputStream fos = new FileOutputStream(file, true)) {
            fos.write(fileAsBytes);
            shareFile(context, file, mimeType);
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "FileNotFoundException", e);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "IOException", ioe);
        }
    }

    /**
     * Decodifica base64.
     */
    public static byte[] decodeBase64(@Nullable String base64) {
        if (base64 != null) {
            try {
                return Base64.decode(base64, 0);
            } catch (Exception e) {
                Log.e(LOG_TAG, "", e);
            }
        }
        return new byte[] {};
    }

    /**
     * Salva um arquivo de base64 e retorna o arquivo criado.
     *
     * @return true apenas se o arquivo for salvo com sucesso.
     */
    public static boolean saveBase64File(@NonNull Activity activity, @NonNull String base64, @NonNull Uri destinationDir) {
        byte[] fileAsBytes = decodeBase64(base64);

        if (fileAsBytes.length == 0) {
            return false;
        }

        try (OutputStream os = activity.getContentResolver().openOutputStream(destinationDir)) {
            os.write(fileAsBytes);
            return true;
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "FileNotFoundException", e);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "IOException", ioe);
        }

        return false;
    }

    /**
     * Remove caracteres ilegais para nomes de arquivos no Android.
     * @return o nome do arquivo filtrado.
     */
    @NonNull
    public static String filterFileName(@Nullable CharSequence fileName) {
        char[] ilegais = {'|', '\\', '?', '*', '<', '\"', ':', '>', '+', '[', ']', '/', '\''};
        StringBuilder sb = new StringBuilder();

        if (fileName != null) {
            for (int i = 0; i < fileName.length(); i++) {
                char fc = fileName.charAt(i);
                boolean shouldWrite = true;

                for (char ic : ilegais) {
                    if (ic == fc) {
                        shouldWrite = false;
                        break;
                    }
                }

                if (shouldWrite) sb.append(fc);
            }
        }
        return sb.toString();
    }

    /**
     * Verifica se o nome do arquivo possui algum extensão.
     * Exemplo: ".pdf"
     * @return true se o nome possuir uma extensão, caso contrário retorna false.
     */
    public static boolean fileNameHasExtension(@Nullable String name) {
        int index = 0;
        if (name != null) index = name.lastIndexOf(".");
        return index > 0 && index < name.length() - 1;
    }

    /**
     * Retorna a extensão a partir do mimeType.
     * A lista aqui não está completa e talvez nunca esteja.
     * Exemplo: mimeType = "application/pdf" Extensão: ".pdf"
     */
    @NonNull
    public static String getExtensionFromMimeType(@Nullable String mimeType) {
        String ext = "";
        if (mimeType != null && mimeType.contains("/")) {
            switch (mimeType) {
                case "image/png":
                    ext = "png";
                    break;
                case "image/jpeg":
                    ext = "jpeg";
                    break;
                case "image/gif":
                    ext = "gif";
                    break;
                case "image/bmp":
                    ext = "bmp";
                    break;
                case "image/svg+xml":
                    ext = "svg";
                    break;
                case "application/pdf":
                    ext = "pdf";
                    break;
                case "text/plain":
                    ext = "txt";
                    break;
                case "text/html":
                    ext = "html";
                    break;
                case "application/msword":
                    ext = "doc";
                    break;
                case "application/vnd.ms-excel":
                    ext = "xls";
                    break;
                case "application/vnd.ms-powerpoint":
                    ext = "ppt";
                    break;
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                    ext = "docx";
                    break;
                case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                    ext = "xlsx";
                    break;
                case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                    ext = "pptx";
                    break;
                default:
                    ext = "";
            }
        }
        return "." + ext;
    }
}

package com.samsungxr.avatar_fashion;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.widgetlib.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import static com.samsungxr.utility.Log.tag;

public class AvatarReader {
    private static final String TAG = tag(AvatarReader.class);
    private SXRContext sxrContext;

    public static final String AVATARS_ROOT = "characters";
    public static final String ANIMATIONS_FOLDER = "animations";
    public static final String MAP_FOLDER = "map";
    public static final String OUTFIT_FOLDER = "outfit";

    public static final String[] MODELS_SUPPORTED = {".fbx", ".dae"};
    public static final String[] ANIMATIONS_SUPPORTED = {".bvh", ".dae"};


    public enum Location {
        sdcard,
        assets
    }

    static boolean isModelFile(String file) {
        Log.d(TAG, "isModelFile file: %s", file);
        for (String ext : MODELS_SUPPORTED) {
            if (file.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    static boolean isAnimFile(String file) {
        Log.d(TAG, "isAnimFile file: %s", file);
        for (String ext : ANIMATIONS_SUPPORTED) {
            if (file.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    boolean isValidAvatar(Location root, String avatar) {
        return getModel(root, avatar) != null && getAnimations(root, avatar) != null;
    }


    private Reader getReader(Location root, String path) {
        Reader reader =  null;
        switch(root) {
            case sdcard:
                reader = new CardReader(path);
                break;
            case assets:
                reader = new AssetsReader(sxrContext.getContext(), path);
                break;
        }
        return reader;
    }


    String[] getAllAvatars(Location root) {
        Log.d(TAG, "getAllAvatars %s", root);
        Reader reader =  getReader(root, AVATARS_ROOT);
        List<String> list = new ArrayList<>();
        if (reader != null) {
            String[] fileNames = reader.getFileNames();
            if (fileNames != null) {
                for (String avatar : fileNames) {
                    Log.d(TAG, "getAllAvatars %s", avatar);
                    if (isValidAvatar(root, avatar)) {
                        list.add(avatar);
                    }
                }
            }
        }

        String[] a = new String[list.size()];
        int i = 0;
        for (String l: list) {
            a[i++] = l;
        }
        return a;
    }

    String getMap(Location root, String avatarName) {
        Reader reader =  getReader(root, AVATARS_ROOT + "/" + avatarName + "/" + MAP_FOLDER);
        String[] list = reader != null ? reader.getFileNames() : null;

        return list != null && list.length > 0 ? reader.readFile(list[0]) : null;
    }

    String getModel(Location root, String avatarName) {
        Reader reader =  getReader(root, AVATARS_ROOT + "/" + avatarName);
        if (reader != null) {
            String[] files = reader.getFileNames();
            if (files != null) {
                for (String file : files) {
                    if (isModelFile(file)) {
                        return reader.getFullPath(file);
                    }
                }
            }
        }
        return null;
    }

    String[] getAnimations(Location root, String avatarName) {
        Reader reader =  getReader(root, AVATARS_ROOT + "/" + avatarName + "/" + ANIMATIONS_FOLDER);
        List<String> list = new ArrayList<>();
        if (reader != null) {
            String[] files  = reader.getFileNames();
            if (files != null) {
                for (String file : files) {
                    if (isAnimFile(file)) {
                        list.add(file);
                    }
                }
            }
        }
        String[] a = new String[list.size()];
        int i = 0;
        for (String l: list) {
            a[i++] = l;
        }
        return a;
    }

    String getAnimation(Location root, String avatarName, String animation) {
        Reader reader =  getReader(root, AVATARS_ROOT + "/" + avatarName + "/" + ANIMATIONS_FOLDER);
        return isAnimFile(animation) ? reader.getFullPath(animation) : null;
    }

    public AvatarReader(SXRContext sxrContext) {
        this.sxrContext = sxrContext;
    }
}

interface Reader {
    String[] getFileNames();
    String readFile(final String fileName);
    String getFullPath(final String fileName);
}

class CardReader implements Reader {
    private final static String sEnvironmentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private final static String EXT_AVATAR_ROOT = "Avatar";

    private String myDirectory;
    private static final String TAG = tag(CardReader.class);

    public CardReader(String sDirectory) {
        myDirectory = sEnvironmentPath + "/" + EXT_AVATAR_ROOT + "/" + sDirectory;
    }

    public String getFullPath(final String fileName) {
        Log.d(TAG, "getFullPath: can read %b", new File(myDirectory + "/" + fileName).canRead());
        return myDirectory + "/" + fileName;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public String[] getFileNames() {
        File directory = new File(myDirectory);
//        directory.setReadable(true, false);

        String[] list = null;
        Log.d(TAG, "getFileNames %s readable =%b", myDirectory, isExternalStorageReadable());
        if (directory.exists() && directory.isDirectory()) {
            Log.d(TAG, "getFileNames directory.length = %b , %d ", directory.canRead(), directory.list() == null ? 0 : directory.list().length);
            File[] files = directory.listFiles();
            if (files != null) {
                list = new String[files.length];
                int i = 0;
                for (File file : files) {
                    Log.d(TAG, "getFileNames file.getName() =  %s", file.getName());
                    list[i++] = file.getName();
                }
            }
        }
        return list;
    }

    public String readFile(final String fileName) {
        try {
            File file = new File(myDirectory + "/" + fileName);
            if (file.exists()) {
                InputStream stream = new FileInputStream(file);
                byte[] bytes = new byte[stream.available()];
                stream.read(bytes);
                String s = new String(bytes);
                Log.d(TAG, "readFile [%s] %s", fileName, s);
                return s;
            }
        } catch (IOException ex) {
            Log.e(TAG, "CardReader: readFile %s not found", fileName);
        }
        return null;
    }
}


class AssetsReader implements Reader {

    private String myDirectory;
    private Context context;
    private static final String TAG = tag(AssetsReader.class);

    AssetsReader(Context context, String sDirectory) {
        myDirectory = sDirectory;
        this.context = context;
    }

    public String getFullPath(final String fileName) {
        return myDirectory + "/" + fileName;
    }

    public String[] getFileNames() {
        String[] list = null;
        try {
            Resources resources = context.getResources();
            AssetManager assetManager = resources.getAssets();
            list = assetManager.list(myDirectory);
        } catch (IOException e) {
            Log.e(TAG, "AssetsReader: getFileNames %s not found", myDirectory);
        }
        return list;
    }

    public String readFile(final String fileName) {
        try {
            SXRAndroidResource res = new SXRAndroidResource(context, myDirectory + "/" + fileName);
            InputStream stream = res.getStream();
            byte[] bytes = new byte[stream.available()];
            stream.read(bytes);
            String s = new String(bytes);
            Log.d(TAG, "readFile [%s] %s", fileName, s);
            return s;
        } catch (IOException ex) {
            Log.e(TAG, "AssetsReader: readFile %s not found", fileName);
            return null;
        }
    }
}

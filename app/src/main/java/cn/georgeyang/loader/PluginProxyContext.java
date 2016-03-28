package cn.georgeyang.loader;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;

import cn.georgeyang.magicbox.R;

/**
 * 可直接加载插件布局和资源的自定义Context包装类
 * @author 小姜
 * @time 2015-4-16 上午11:03:47
 */
public class PluginProxyContext extends ContextWrapper {

	private AssetManager mAssetManager = null;
	private Resources mResources = null;
	private LayoutInflater mLayoutInflater = null;
	private Theme mTheme = null;
	private static final HashMap<String, PluginProxyContext> plugLoderContent = new HashMap<>();
	private PlugClassLoder mClassLoder = null;

	private String packageName = null;
	
	// *****************资源ID类型*******************
    public static final String LAYOUT = "layout";
    public static final String ID = "id";
    public static final String DRAWABLE = "drawable";
    public static final String STYLE = "style";
    public static final String STRING = "string";
    public static final String COLOR = "color";
    public static final String DIMEN = "dimen";

	public PluginProxyContext(Context base,String pluginPath) {
		super(base);

		try {
			File outFile = new File(pluginPath);
			AssetManager assetManager = AssetManager.class.newInstance();
			Method addAssetPath = assetManager.getClass().getMethod(
					"addAssetPath", String.class);
			addAssetPath.invoke(assetManager, outFile.getPath());
			mAssetManager = assetManager;
		} catch (Exception e) {
			Log.d("demo",Log.getStackTraceString(e).toString());
			e.printStackTrace();
		}
		Resources superRes = super.getResources();
		mResources = new Resources(mAssetManager, superRes.getDisplayMetrics(),
				superRes.getConfiguration());


//		this.packageName = mResources.getResourcePackageName(R.string.app_name);//获取插件包名
//		packageName = getString("app_name");
		this.packageName = mResources.getResourcePackageName(2131099648);
		Log.d("demo","packageName:" + packageName);
//		this.packageName = "pName";

		mClassLoder = new PlugClassLoder(base.getFilesDir() + "/dexOut",pluginPath,null,base.getClassLoader());
		getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		plugLoderContent.put(new File(pluginPath).getName(),this);
	}
	
	/**
	 * 单例模式(单利模式会有缓存，所以不使用单例)
	 *
	 * @param context
	 * @return
	 * @author 小姜
	 * @time 2015-4-16 上午11:30:37
	 *//*
	private static PluginProxyContext proxyContext;
	public static PluginProxyContext getInstance(Context context){
		if(proxyContext == null){
			proxyContext = new PluginProxyContext(context);
		}
		return proxyContext;
	}*/
//	/**
//	 * 加载插件中的资源
//	 *
//	 * @param pluginPackageName 资源插件的包名
//	 * @param testModel 是否为测试模式。 如果是测试模式每次都会拷贝assets插件到项目文件目录中，保存插件每次都是最新的，上线时请关闭
//	 * @author 小姜
//	 * @time 2015-4-16 上午11:31:36
//	 */
//	public void loadResources(String resPluginName, boolean testModel) {
//
//	}

	
	/**
	 * 获取插件资源对应的id
	 *
	 * @param type
	 * @param name
	 * @return
	 * @author 小姜
	 * @time 2015-4-16 上午11:31:56
	 */
	public int getIdentifier(String type, String name){
		return mResources.getIdentifier(name, type, packageName);
	}



	public int getId(String name){
		return mResources.getIdentifier(name, ID, packageName);
	}
	/**
	 * 获取插件中的layout布局
	 *
	 * @param name
	 * @return
	 * @author 小姜
	 * @time 2015-4-16 上午11:32:12
	 */
	public View getLayout(String name){
		return mLayoutInflater.inflate(getIdentifier(LAYOUT,name), null);
	}
	public String getString(String name){
		return mResources.getString(getIdentifier(STRING, name));
	}
	public int getColor(String name){
		return mResources.getColor(getIdentifier(COLOR, name));
	}
	public Drawable getDrawable(String name){
		return mResources.getDrawable(getIdentifier(DRAWABLE, name));
	}
	public int getStyle(String name){
		return getIdentifier(STYLE, name);
	}
	public float getDimen(String name){
		return mResources.getDimension(getIdentifier(DIMEN, name));
	}

	
	/**
	 * 创建一个当前类的布局加载器，用于专门加载插件资源
	 */
	@Override
	public Object getSystemService(String name) {
		if (LAYOUT_INFLATER_SERVICE.equals(name)) {
			if (mLayoutInflater == null) {
				try {
					Class<?> cls = Class
							.forName("com.android.internal.policy.PolicyManager");
					Method m = cls.getMethod("makeNewLayoutInflater",
							Context.class);
					//传入当前PluginProxyContext类实例，创建一个布局加载器
					mLayoutInflater = (LayoutInflater) m.invoke(null, this);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}else {
				return mLayoutInflater;
			}
		}
		return super.getSystemService(name);
	}

	
	@Override
	public AssetManager getAssets() {
		return mAssetManager;
	}
	
	@Override
	public Resources getResources() {
		return mResources;
	}
	
	@Override
	public ClassLoader getClassLoader() {
		return mClassLoder;
	}
	
	@Override
	public Resources.Theme getTheme() {
		if(mTheme == null){
			mTheme = mResources.newTheme();
			mTheme.applyStyle(android.R.style.Theme_Light,true);
		}
		return mTheme;
	}
	
	
	@Override
	public String getPackageName() {
		return packageName;
	}
	


}

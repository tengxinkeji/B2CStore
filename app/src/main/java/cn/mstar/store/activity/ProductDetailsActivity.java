package cn.mstar.store.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.mstar.store.R;
import cn.mstar.store.app.Constants2;
import cn.mstar.store.app.MyAction;
import cn.mstar.store.app.MyApplication;
import cn.mstar.store.entity.BuyProductEntity;
import cn.mstar.store.entity.Product;
import cn.mstar.store.fragments.ProductDetailsBottomFragment;
import cn.mstar.store.interfaces.OnResultStatusListener;
import cn.mstar.store.utils.Utils;
import cn.mstar.store.utils.CustomToast;
import cn.mstar.store.utils.ImageLoadOptions;
import cn.mstar.store.utils.L;
import cn.mstar.store.utils.NetWorkUtil;
import cn.mstar.store.utils.VolleyRequest;
import cn.mstar.store.utils.VolleyRequest.HttpStringRequsetCallBack;
import cn.mstar.store.utils.VolleyRequest.LogonStatusLinstener;
import cn.mstar.store.customviews.LoadingDialog;
import cn.mstar.store.customviews.SharePopup;
import cn.mstar.store.customviews.SharePopup.OnDialogListener;

/**
 * 产品详情界面
 *
 * @author wenjundu 2015-7-17
 *
 */
public class ProductDetailsActivity extends AppCompatActivity implements
		OnDialogListener, OnClickListener {
	public static final int SET_TO_FAVORITE_SUCCESS = 221;
	public static final int SET_TO_FAVORITE_FAILURE = 222;
	// 分享按钮
	private ImageView shareIV;
	// 分享窗口
	private SharePopup popup;
	private RelativeLayout layout_root;
	private Product product;
	private String productinfoURL;
	private ViewPager viewPager;
	private LayoutInflater inflater;
	private View item;
	private ImageView image;
	private MyAdapter adapter;// viewpager适配器
	private ImageView titleBack;
	private TextView titleName;
	private TextView productName, productPrice;// 产品名称,价格
	private LinearLayout normsLayout;// 规格行
	private TextView normsTV;// 规格文字
	private TextView btnAddcart, btnBuyNow;// 加入购物车 立即购买
	private String norm = "";// 规格字符串
	private int buyNum = 1;// 购买数量默认1
	private TextView indicatorTV;//展示图片指示器
	private int[] checkedIds;
	private ImageView iv_collect;

	private boolean hasContentChanged = false;
	private BuyProductEntity buyProductEntity;
	private MyApplication app;
	@Bind(R.id.frame_product_details_viewpager) FrameLayout frame_product_details_viewpager;



	public void loginToServer() {
		Intent loginIntent = new Intent(this, LoginActivity.class);
		startActivity(loginIntent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product_details);
		ButterKnife.bind(this);
		MyApplication.getInstance().addActivity(this);
		app = (MyApplication) getApplication();
		Utils.setNavigationBarColor(this, getResources().getColor(R.color.status_bar_color));
		Utils.setStatusBarColor(this, getResources().getColor(R.color.status_bar_color));
		initView();
		initData();
		// we will init it with the gotten data.
//		initBottomFragment ();
	}


	@Override
	protected void onResume() {
		super.onResume();
		i_dismissProgressDialog();
	}

	private void initData() {
		// TODO Auto-generated method stub

		Intent intent = getIntent();
		if (MyAction.productListActivityAction.equals(intent.getAction())) {
			int proId = intent.getExtras().getInt("proId");
			productinfoURL = Constants2.PRODUCTDETAIL_URL + "&proid=" + proId;
		}
		L.e("productinfoURL", productinfoURL);
		//  标题
		titleName.setText(getString(R.string.product_details));
		getproductInfo(productinfoURL);
	}

	// 获得产品信息
	private void getproductInfo(String productinfoURL) {

		// size
		productinfoURL+="&size="+(getScreenWidth() < 700 ? "240" : "360");
		if (NetWorkUtil.isNetworkConnected(this)) {
			i_showProgressDialog();
			VolleyRequest.GetCookieRequestPurePage(ProductDetailsActivity.this, productinfoURL, new HttpStringRequsetCallBack() {
				@Override
				public void onSuccess(String result) {
					try {
						JSONObject jsonObject = new JSONObject(result);
						JSONObject dataJson = jsonObject
								.getJSONObject("data");
						product = new Product();
						product.setProId(dataJson
								.getInt("proId"));
						product.setName(dataJson.getString("name"));
						product.setDescURL(dataJson
								.getString("desc"));
						product.setImageUrl(dataJson
								.getString("pic"));
						product.setAttributes(dataJson
								.getString("attribute"));
						product.setPrice(Double
								.parseDouble(dataJson
										.getString("price")));
						product.setKindName(dataJson
								.getString("kindName"));
						product.setWeight(dataJson
								.getString("weight"));
						product.setBrandName(dataJson
								.getString("brandName"));
						product.setArea(dataJson.getString("area"));
						product.setHit(dataJson.getString("hit"));
						product.setProNum(dataJson
								.getString("proNum"));
						product.setHaveProSpecificationPrice(Boolean.parseBoolean(dataJson
								.getString("isHaveProSpecificationPrice")));
						product.setStock(dataJson.optInt("stock"));
						product.setHaveProFavorite(Boolean.parseBoolean(dataJson.getString("isHaveproFavorite")));
						JSONArray picsJson = dataJson
								.getJSONArray("pics");
						String[] pics = new String[picsJson
								.length()];
						for (int i = 0; i < picsJson.length(); i++) {
							pics[i] = picsJson.optString(i);
						}
						product.setPics(pics);
						// 往页面填充数据
						L.d(" --- " + product.toString());
						LoadingData(product);
					} catch (Exception e) {
						CustomToast.makeToast(ProductDetailsActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT);
						finish();
					}
					i_dismissProgressDialog();
				}

				@Override
				public void onFail(String error) {
					CustomToast.makeToast(ProductDetailsActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT);
					i_dismissProgressDialog();
					finish();
				}
			});
		}
	}


	@SuppressLint("InflateParams")
	private void LoadingData(Product product) {
		// TODO Auto-generated method stub
		final List<View> list = new ArrayList<View>();
		inflater = LayoutInflater.from(this);
		/**
		 * 创建多个item （每一条viewPager都是一个item） 从服务器获取完数据（图片url地址） 后，再设置适配器
		 */
		for (int i = 0; i < product.getPics().length; i++) {
			item = inflater.inflate(R.layout.item_product_viewpager, null);
			list.add(item);
		}
		// 创建适配器， 把组装完的组件传递进去
		adapter = new MyAdapter(list);
		L.e(viewPager.toString());
		viewPager.setAdapter(adapter);
		indicatorTV.setVisibility(View.VISIBLE);
		indicatorTV.setText("1" + "/" + list.size());
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				indicatorTV.setText(position + 1 + "/" + list.size());
				L.e("" + position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});
		if (product != null)
			L.d("pro::", product.toString());
		// 产品名称
		productName.setText(product.getName());
		// 产品价格
		productPrice.setText(Utils.formatedPrice(product.getPrice()));
		// 默认规格字
		if (product.isHaveProSpecificationPrice()) {
			normsTV.setText(getString(R.string.select_norms));
//			iv_collect.setBackgroundResource(R.drawable.btn_collect_pressed);
		} else {
			normsTV.setText(getString(R.string.no_norms));
//			iv_collect.setBackgroundResource(R.drawable.btn_collect_normal);
		}

		if (product.isHaveProFavorite()) {
			iv_collect.setBackgroundResource(R.drawable.btn_collect_pressed);
		} else
			iv_collect.setBackgroundResource(R.drawable.btn_collect_normal);

		// loading the webview.
		if (product != null)
			initBottomFragment (product.getDescURL(), product.getAttributes());
	}

	private void initBottomFragment(String descURL, String attributes) {


		L.d("desc:::",descURL);
		//
		ProductDetailsBottomFragment frg = ProductDetailsBottomFragment.getInstance(descURL, attributes, getScreenHeight());
//		frame_product_details_viewpager
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//		transaction.setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out);
		// 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
		transaction.add(R.id.frame_product_details_viewpager, frg);
		transaction.commit();
	}

	private int getScreenHeight() {

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.heightPixels;
	}



	private int getScreenWidth() {

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.widthPixels;
	}


	LoadingDialog dialog;
	public void i_showProgressDialog() {
		dialog = new LoadingDialog(this);
		dialog.show();
	}

	public void i_dismissProgressDialog () {
		if (dialog != null) {
			dialog.cancel();
			dialog.dismiss();
			dialog = null;
		}
	}

	private void initView() {
		// TODO Auto-generated method stu

		shareIV = (ImageView) findViewById(R.id.iv_share);
		layout_root = (RelativeLayout) findViewById(R.id.layout_root);
		viewPager = (ViewPager) findViewById(R.id.viewPager);

		titleBack = (ImageView) findViewById(R.id.title_back);
		titleName = (TextView) findViewById(R.id.title_name);

		shareIV = (ImageView) findViewById(R.id.iv_share);
		layout_root = (RelativeLayout) findViewById(R.id.layout_root);
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		titleBack.setVisibility(View.VISIBLE);
		titleBack.setOnClickListener(this);
		productName = (TextView) findViewById(R.id.tv_product_name);
		productPrice = (TextView) findViewById(R.id.tv_price);

		btnAddcart = (TextView) findViewById(R.id.btn_add_shopping_cart);
		btnBuyNow = (TextView) findViewById(R.id.btn_buy_now_cart);

		indicatorTV=(TextView) findViewById(R.id.indicator_tv);

		btnAddcart.setOnClickListener(this);
		btnBuyNow.setOnClickListener(this);
		normsLayout = (LinearLayout) findViewById(R.id.norms_layout);
		normsTV = (TextView) findViewById(R.id.tv_norms);
		popup = new SharePopup(this, this);







	/*
		backgroundAlpha(1f);
		// 添加pop窗口关闭事件
		popup.setOnDismissListener(new poponDismissListener());
		shareIV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				popup.showAtLocation(layout_root, Gravity.BOTTOM, 0, 0);
				backgroundAlpha(0.7f);
			}
		});

		*/







		normsLayout.setOnClickListener(this);
		iv_collect = (ImageView) findViewById(R.id.iv_collect);
		iv_collect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				L.d(" --- got object "+ product.toString() );
				//				SetProductToFavoriteThread task = new SetProductToFavoriteThread(product.getProId(),
				//						!product.isHaveProFavorite(), mHandler/* get it */);



				VolleyRequest.checkLogStatus((MyApplication) ProductDetailsActivity.this.getApplication(), new LogonStatusLinstener() {

					@Override
					public void OK(String reason) {
						// TODO Auto-generated method stub

						// a method to delete or add the items.
						if (product.isHaveProFavorite())
							VolleyRequest.deleteItemsFromFavorite(ProductDetailsActivity.this, String.valueOf(product.getProId()),Utils.getTokenKey((MyApplication) ProductDetailsActivity.this.getApplication()), new OnResultStatusListener() {

								@Override
								public void success(String result) {

									product.setHaveProFavorite(!product.isHaveProFavorite());
									hasContentChanged = !hasContentChanged;
									if (product.isHaveProFavorite()) {
										iv_collect
												.setBackgroundResource(R.drawable.btn_collect_pressed);
										//										CustomToast.mToast(ProductDetailsActivity.this, "adding ok");
										CustomToast.mSystemToast(ProductDetailsActivity.this, getString(R.string.delete_favorite_error));
									} else {
										iv_collect
												.setBackgroundResource(R.drawable.btn_collect_normal);
										//										CustomToast.mToast(ProductDetailsActivity.this, "deleting ok");
										CustomToast.mSystemToast(ProductDetailsActivity.this, getString(R.string.delete_favorite_ok));
									}
								}

								@Override
								public void failure(String error) {
									CustomToast.mSystemToast(ProductDetailsActivity.this, error);
								}
							});
						else {
							VolleyRequest.addItemsFromFavorite(ProductDetailsActivity.this, String.valueOf(product.getProId()), Utils.getTokenKey((MyApplication) ProductDetailsActivity.this.getApplication()),new OnResultStatusListener() {

								@Override
								public void success(String result) {

									product.setHaveProFavorite(!product.isHaveProFavorite());
									hasContentChanged = !hasContentChanged;
									if (product.isHaveProFavorite()) {
										iv_collect
												.setBackgroundResource(R.drawable.btn_collect_pressed);
										CustomToast.mSystemToast(ProductDetailsActivity.this, getString(R.string.adding_favorite_ok));
									} else {
										iv_collect
												.setBackgroundResource(R.drawable.btn_collect_normal);
										CustomToast.mSystemToast(ProductDetailsActivity.this, getString(R.string.adding_favorite_error));
									}
								}

								@Override
								public void failure(String error) {
									CustomToast.mSystemToast(ProductDetailsActivity.this, error);
								}
							});
						}
					}

					@Override
					public void NO() {

						// 需要首先登陆
						loginToServer();
					}
				});


			}
		});
	}

	@Override
	public void onShareWechat() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onShareFriends() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onShareSina() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onShareQQ() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onShareQQZone() {
		// TODO Auto-generated method stub

	}

	/**
	 * 适配器，负责装配 、销毁 数据 和 组件 。
	 */
	private class MyAdapter extends PagerAdapter {
		private List<View> mList;

		public MyAdapter(List<View> list) {
			mList = list;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mList.size();
		}

		/**
		 * Remove a page for the given position. 滑动过后就销毁 ，销毁当前页的前一个的前一个的页！
		 * instantiateItem(View container, int position) This method was
		 * deprecated in API level . Use instantiateItem(ViewGroup, int)
		 */
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			container.removeView(mList.get(position));
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			// TODO Auto-generated method stub

			View view = mList.get(position);
			image = ((ImageView) view.findViewById(R.id.image));
			ImageLoader.getInstance().displayImage(product.getPics()[position],
					image, ImageLoadOptions.getOptions());
			image.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(ProductDetailsActivity.this,
							ImageBrowserActivity.class);
					intent.putExtra("photos", product.getPics());
					L.e("size:"+product.getPics().length);
					intent.putExtra("position", position);
					startActivity(intent);
				}
			});
			container.removeView(mList.get(position));
			container.addView(mList.get(position));
			return mList.get(position);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		switch (resultCode) {
			case 1:
				buyProductEntity = (BuyProductEntity)data.getSerializableExtra("buyProduct");
				if (buyProductEntity != null)
					L.d("buy:::", buyProductEntity.toString());
				product.setPrice(buyProductEntity.getProduct().getPrice());
				product.setStock(buyProductEntity.getProduct().getStock());
				product.setProSpecialID(buyProductEntity.getProduct().getProSpecialID());
				buyProductEntity.setProduct(product);
				productPrice.setText(Utils.formatedPrice(product.getPrice()));
				// 不让改变选择规格那部分二
//				normsTV.setText(buyProductEntity.getNorms());
				checkedIds=data.getIntArrayExtra("checkedIds");
				buyNum = buyProductEntity.getBuyNum();
				break;
		}
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		Intent i = new Intent();
		//                                          i.putExtra(Constants.START_ACTIVITY_FOR_RESULT_KEY, true);
		i.putExtra(cn.mstar.store.utils.Constants.START_ACTIVITY_FOR_RESULT_KEY, hasContentChanged);
		this.setResult(2, i);
		super.finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.title_back:// 返回
				finish();
				break;
			case R.id.norms_layout:// 跳转到产品规格页
			/*	if (product != null) {
					if (product.isHaveProSpecificationPrice()) {// 有规格
						Intent intent = new Intent(this,
								SelectCommodityActivity.class);
						if(checkedIds!=null)
							intent.putExtra("checkedIds",checkedIds);
						if(buyProductEntity!=null)
							intent.putExtra("buyNums",buyProductEntity.getBuyNum());
						else
							intent.putExtra("buyNums",1);
						intent.putExtra("proId", product.getProId());
						intent.setAction(MyAction.productDetailsActivityAction);
						// startActivity(intent);
						startActivityForResult(intent, 1);
					} else {// 无规格
						CustomToast.makeToast(this, getString(R.string.no_norms),
								Toast.LENGTH_SHORT);
					}
				}*/
				addElementToShoppingCart();
				break;
			case R.id.btn_add_shopping_cart:// 加入购物车
				// add elements to shopping cart.
				addElementToShoppingCart();
				break;
			case R.id.btn_buy_now_cart:// 立即购买
				if(product.getStock()==0){
					CustomToast.makeToast(this,"库存不足",Toast.LENGTH_SHORT);
					return;
				}

				if ("".equals(app.tokenKey)) {
					loginToServer();
					return;
				}
				// 直接调到选择规格见面，然后
				Intent intent = new Intent(this,
						SelectCommodityActivity.class);
				intent.putExtra("proId", product.getProId());
				intent.setAction(MyAction.directlyGetToPayAction);
				startActivity(intent);

			/*	if (product != null && product.isHaveProSpecificationPrice()) {// 是否有规格
					if (buyProductEntity!=null && !"".equals(buyProductEntity.getNorms())) {// 是否选择了规格
						// 去购买
						Intent intent = new Intent(
								ProductDetailsActivity.this,
								ConfirmIndentActivity.class);
						intent.setAction(MyAction.productDetailsActivityAction);
						intent.putExtra("buyProduct", buyProductEntity);
						startActivity(intent);


					} else {// 去规格页选择规格
						Intent intent = new Intent(this,
								SelectCommodityActivity.class);
						intent.putExtra("proId", product.getProId());
						intent.setAction(MyAction.productDetailsActivityAction);
						startActivityForResult(intent, 1);
					}
				} else {
					// 无其他规格，直接进入订单页
					Intent intent = new Intent(
							ProductDetailsActivity.this,
							ConfirmIndentActivity.class);
					intent.setAction(MyAction.productDetailsActivityAction);
					BuyProductEntity buyProductEntity=new BuyProductEntity();
					buyProductEntity.setBuyNum(buyNum);//设置购买数量
					buyProductEntity.setProduct(product);//产品信息
					buyProductEntity.setNorms(norm);//规格参数
					intent.putExtra("buyProduct", buyProductEntity);
					startActivity(intent);
				}*/
				break;
		}
	}

	private void addElementToShoppingCart() {

		if (app == null || (app != null && "".equals(app.tokenKey))) {
			loginToServer();
		return;
		}
	/*	i_showProgressDialog();
		VolleyRequest.checkLogStatus((MyApplication) getApplication(), new LogonStatusLinstener() {
			@Override
			public void OK(String reason) {
				// 登录~可以发出去请求
				L.d("登录后~  spec " + product.getProSpecialID() + " proId " + product.getProId() + " count = " + buyNum);
				ProAndSpecialIdz item = new ProAndSpecialIdz(product.getProId(), product.getProSpecialID(), buyNum);
				VolleyRequest.addInShoppingCart((MyApplication) ProductDetailsActivity.this.getApplication(), new ProAndSpecialIdz[]{item}, Utils.getTokenKey((MyApplication) ProductDetailsActivity.this.getApplication()), new OnResultStatusListener() {
					@Override
					public void success(String result) {
						// 添加成功
						CustomToast.makeToast(ProductDetailsActivity.this, getString(R.string.add_shopping_cart_success), Toast.LENGTH_SHORT);
						i_dismissProgressDialog();
					}

					@Override
					public void failure(String error) {
						switch (error) {
							case "0":
								// 添加失败
								CustomToast.makeToast(ProductDetailsActivity.this, getString(R.string.add_shopping_cart_failure), Toast.LENGTH_SHORT);
								break;
							case "-1":
								// 网络失败
								CustomToast.makeToast(ProductDetailsActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT);
								break;
							default:
								CustomToast.makeToast(ProductDetailsActivity.this, error, Toast.LENGTH_SHORT);
								break;
						}
						i_dismissProgressDialog();
					}
				});

			}

			@Override
			public void NO() {
				// 未登录~不发什么
//				L.d("未登录~  spec " + product.getProSpecialID() + " proId " + product.getProId());
				*//*ProAndSpecialIdz item = new ProAndSpecialIdz(product.getProId(), product.getProSpecialID(), buyNum);
				item.save();
				CustomToast.makeToast(ProductDetailsActivity.this, getString(R.string.add_shopping_cart_local_success), Toast.LENGTH_SHORT);
				dismissDialog_();*//*
				loginToServer();
			}
		});*/

		// start the other activity with the add item to cart
//		if (getIntent().getAction().equals(MyAction.addToChartAction)) {
//		Intent intent = new Intent()
		if (product != null) {
			Intent intent = new Intent(this,
					SelectCommodityActivity.class);
			if(checkedIds!=null)
				intent.putExtra("checkedIds",checkedIds);
			if(buyProductEntity!=null)
				intent.putExtra("buyNums",buyProductEntity.getBuyNum());
			else
				intent.putExtra("buyNums",1);
			intent.putExtra("proId", product.getProId());
			intent.setAction(MyAction.productDetailsActivityAction);
			// startActivity(intent);
			startActivityForResult(intent, 1);
		}
	}


	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		overridePendingTransition(R.anim.custom_in_anim, R.anim.custom_out_anim);
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
		overridePendingTransition(R.anim.custom_in_anim, R.anim.custom_out_anim);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		MyApplication.requestQueue.cancelAll(this);
		super.onDestroy();
	}
}

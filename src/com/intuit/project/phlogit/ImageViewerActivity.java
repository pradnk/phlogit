package com.intuit.project.phlogit;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.intuit.project.phlogit.data.vo.FacebookCommentsLike;
import com.intuit.project.phlogit.data.vo.Profile;
import com.intuit.project.phlogit.service.SyncService;
import com.intuit.project.phlogit.socialnetwork.FacebookSocialNetwork;

public class ImageViewerActivity extends Activity {
	private ViewGroup layout;
	private String facebookId;
	private ViewGroup commentsListSection;
	private ViewGroup toggleSection;
	private TextView likes;
	private FacebookSocialNetwork facebook;
	private ImageView imageView;
	private String photoGalleryId;
	private ViewGroup syncSection;
	private TextView sync;
	private ViewGroup commentsSection;
	private ViewGroup likesSection;
	private ProgressDialog dialog;
	private ProgressBar progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.image_viewer);
		
		final String photoId = getIntent().getStringExtra("ID");
		final String photoParentId = getIntent().getStringExtra("PARENT_ID");
		final String facebookId = getIntent().getStringExtra("FACEBOOK_ID");
		final String galleryId = getIntent().getStringExtra("GALLERY_ID");
		final String synced = getIntent().getStringExtra("SYNCED");
		
		imageView = (ImageView) findViewById(R.id.image);
		progress = (ProgressBar) findViewById(R.id.progress);
		likes = (TextView) findViewById(R.id.likes);
		commentsListSection = (ViewGroup) findViewById(R.id.commentsListSection);
		commentsSection = (ViewGroup) findViewById(R.id.commentsSection);
		likesSection = (ViewGroup) findViewById(R.id.likesSection);
		syncSection = (ViewGroup) findViewById(R.id.syncSection);
		sync = (TextView) findViewById(R.id.sync);
		
		if("true".equals(synced)) {
			syncSection.removeAllViews();
			syncSection.setVisibility(View.GONE);
		} else {
			likesSection.setVisibility(View.GONE);
			commentsSection.setVisibility(View.GONE);
			syncSection.setVisibility(View.VISIBLE);
			LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
			params.gravity = Gravity.CENTER_HORIZONTAL;
			syncSection.setLayoutParams(params);
		}
		
		syncSection.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ImageViewerActivity.this, SyncService.class);
				intent.putExtra("ID", photoId);
				intent.putExtra("PARENT_ID", photoParentId);
				startService(intent);
			}
		});
		
		toggleSection = (ViewGroup) findViewById(R.id.toggle);
		
		final ListView list = (ListView)findViewById(R.id.commentsList);
		final EditText addComment = (EditText)findViewById(R.id.addComment);
		final Button addCommentBtn = (Button)findViewById(R.id.addCommentBtn);
		
		final TextView comments  = (TextView)findViewById(R.id.comments);
		
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				File file = new File(galleryId);
				Uri uri = Uri.fromFile(file);
				imageView.setImageURI(uri);
				Looper.loop();
			}
		}.start();

		layout = (ViewGroup) findViewById(R.id.layout);
		layout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(toggleSection.getVisibility() == View.INVISIBLE) {
					toggleSection.setVisibility(View.VISIBLE);
					if("true".equals(synced)) {
					}
				} else {
					toggleSection.setVisibility(View.INVISIBLE);
				}
				if(commentsListSection != null && commentsListSection.getVisibility() == View.VISIBLE) {
					commentsListSection.setVisibility(View.GONE);
				}
			}
		});
		
		list.setCacheColorHint(0);
		list.setDivider(getResources().getDrawable(R.drawable.horizontal_separator));

		facebook = new FacebookSocialNetwork(getApplicationContext());
		
		final FacebookCommentsLike commentsLike  = facebook.getComments(facebookId);
		list.setAdapter(new CommentsAdapter(getApplicationContext(), commentsLike));
		list.setClickable(false);
		likes.setText(" Like ("+commentsLike.likeNumber+")");

		likes.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(commentsLike != null) {
					showLikePopup(commentsLike);
				}
			}
		});
		
		comments.setText(" Comments ("+commentsLike.comments.size()+")");
		addCommentBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String comment = addComment.getText().toString();
				facebook.addComment(facebookId, comment);
				addComment.setText("");
				commentsLike.comments.add(comment);
				Profile profile = facebook.getProfile();
				if(profile != null) {
					commentsLike.from.add(profile.name);
				}
				CommentsAdapter adapter = new CommentsAdapter(getApplicationContext(), commentsLike);
				adapter.notifyDataSetChanged();
				list.setAdapter(adapter);
				comments.setText(" Comments ("+commentsLike.comments.size()+")");
			}
		});
		
		comments.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				commentsListSection.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_up_in));
				commentsListSection.setVisibility(View.VISIBLE);
			}
		});
		
		commentsListSection.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(commentsListSection.getVisibility() == View.VISIBLE) {
					commentsListSection.setVisibility(View.GONE);
				}
			}
		});

		handler.sendEmptyMessageDelayed(100, 1500);
	}
	
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 100:
				progress.setVisibility(View.GONE);
				imageView.setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}
		}
	};

	private void showLikePopup(FacebookCommentsLike commentsLike) {
		final PopupWindow popup = new PopupWindow(this);
		popup.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg));
		popup.setTouchable(true);
		popup.setFocusable(true);
		popup.setOutsideTouchable(true);
		popup.setWidth(200);
		popup.setHeight(200);
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup mainView = (ViewGroup)inflater.inflate(R.layout.popup_like, null);
		TextView likedBy = (TextView)mainView.findViewById(R.id.likedBy);
		ImageView like = (ImageView)mainView.findViewById(R.id.like);
		ImageView unlike = (ImageView)mainView.findViewById(R.id.unLike);
		
		like.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				facebook.like(facebookId);
			}
		});
		
		unlike.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				facebook.unLike(facebookId);
			}
		});
		
		likedBy.setText(commentsLike.likedBy);
		popup.setContentView(mainView);
		Rect rect = new Rect();
		likes.getGlobalVisibleRect(rect);
		popup.setAnimationStyle(R.anim.push_up_in);
		popup.showAsDropDown(likes, 10, 10);
	}
	
	/**
     * Adapter for our image files.
     */
    private class CommentsAdapter extends BaseAdapter {

        private Context context;
		private FacebookCommentsLike commentsLike;
		private LayoutInflater layoutInflater;

        public CommentsAdapter(Context localContext, FacebookCommentsLike commentsLike) {
            context = localContext;
            this.commentsLike = commentsLike;
            layoutInflater = (LayoutInflater)localContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return commentsLike.comments.size();
        }
        
        public Object getItem(int position) {
            return position;
        }
        
        public long getItemId(int position) {
            return position;
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
        	ViewHolder holder;
            if (convertView == null) {
                convertView = (LinearLayout)layoutInflater.inflate(R.layout.comments_list_item, null);
                holder = new ViewHolder();
                holder.comment = (TextView)convertView.findViewById(R.id.comment);
                holder.from = (TextView)convertView.findViewById(R.id.from);
                holder.comment.setTextSize(14);
                holder.comment.setTextColor(Color.WHITE);
                holder.from.setTextSize(15);
                holder.from.setTextColor(Color.WHITE);
                holder.from.setTypeface(Typeface.DEFAULT_BOLD);
                convertView.setTag(holder);
            }
            else {     
                holder = (ViewHolder) convertView.getTag();
            }
            holder.comment.setText(commentsLike.comments.get(position));
            holder.from.setText(commentsLike.from.get(position));
            return convertView;
        }
    }
    
    class ViewHolder {
    	TextView comment;
    	TextView from;
    }
    
    @Override
    public void onBackPressed() {
    	if(commentsListSection != null && commentsListSection.getVisibility() == View.VISIBLE) {
    		commentsListSection.setVisibility(View.GONE);
    		return;
    	}
    	super.onBackPressed();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    }
}
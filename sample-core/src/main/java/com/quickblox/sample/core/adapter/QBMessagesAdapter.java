package com.quickblox.sample.core.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.sample.core.R;
import com.quickblox.users.model.QBUser;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QBMessagesAdapter extends RecyclerView.Adapter<QBMessagesAdapter.QBMessageViewHolder> implements QBBaseAdapter<QBChatMessage> {
    private static final String TAG = QBMessagesAdapter.class.getSimpleName();

    private int typeOwnAttachmentMessageLayoutResource = R.layout.item_attachment_message_own;
    private int typeOpponentAttachmentMessageLayoutResource = R.layout.item_attachment_message_opponent;
    private int typeOwnMessageLayoutResource = R.layout.item_text_message_own;
    private int typeOpponentMessageLayoutResource = R.layout.item_text_message_opponent;

    private String textOwn;
    private String timeOwn;
    private String textOpp;
    private String timeOpp;

    private float textOwnSize;
    private int textOwnColor;

    private int preferredImageSizePreview = (int) (80 * Resources.getSystem().getDisplayMetrics().density);
    private RequestListener glideRequestListener;
    private QBMessageViewHolder qbViewHolder;
    private boolean useEmbeddedImageLoader = true;
    private
    @LayoutRes
    int widgetLayoutResId;

    protected enum ViewTypes {TYPE_OWN_MESSAGE, TYPE_OPPONENT_MESSAGE, TYPE_ATTACHMENT_MESSAGE_OWN, TYPE_ATTACHMENT_MESSAGE_OPPONENT, TYPE_ATTACHMENT_CUSTOM}

    protected List<QBChatMessage> chatMessages;
    protected LayoutInflater inflater;
    protected Context context;

    protected QBMessageViewHolder customHolder;


    public QBMessagesAdapter(Context context, List<QBChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public QBMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewTypes valueType = ViewTypes.values()[viewType];
        switch (valueType) {
            case TYPE_OWN_MESSAGE:
                qbViewHolder = new MessageOwnHolder(inflater.inflate(typeOwnMessageLayoutResource, parent, false), inflater, widgetLayoutResId, R.id.message_textview, R.id.time_text_message_textview);
                return qbViewHolder;
            case TYPE_OPPONENT_MESSAGE:
                qbViewHolder = new MessageOpponentHolder(inflater.inflate(typeOpponentMessageLayoutResource, parent, false), R.id.message_textview, R.id.time_text_message_textview);
                return qbViewHolder;
            case TYPE_ATTACHMENT_MESSAGE_OWN:
                qbViewHolder = new AttachOwnHolder(inflater.inflate(typeOwnAttachmentMessageLayoutResource, parent, false), R.id.attach_imageview, R.id.centered_progressbar);
                return qbViewHolder;
            case TYPE_ATTACHMENT_MESSAGE_OPPONENT:
                qbViewHolder = new AttachOpponentHolder(inflater.inflate(typeOpponentAttachmentMessageLayoutResource, parent, false), R.id.attach_imageview, R.id.centered_progressbar);

            case TYPE_ATTACHMENT_CUSTOM:
                Log.d(TAG, "onCreateViewHolder case TYPE_ATTACHMENT_CUSTOM");
                // resource must be set manually by creating custom adapter
//                ToDo temporary stub потом customHolder будет создаваться в CustomAdapter на клиенте
                return customHolder = new AttachOpponentHolder(inflater.inflate(typeOpponentAttachmentMessageLayoutResource, parent, false), R.id.attach_imageview, R.id.centered_progressbar);
            default:
                return null;
        }
    }

    protected void setOwnMessageLayoutResource(@LayoutRes int typeOwnMessageLayoutResource) {
        this.typeOwnMessageLayoutResource = typeOwnMessageLayoutResource;
    }

    protected void setOpponentMessageLayoutResource(@LayoutRes int typeOpponentMessageLayoutResource) {
        this.typeOpponentMessageLayoutResource = typeOpponentMessageLayoutResource;
    }

    protected void setOwnAttachmentMessageLayoutResource(@LayoutRes int typeOwnAttachmentMessageLayoutResource) {
        this.typeOwnAttachmentMessageLayoutResource = typeOwnAttachmentMessageLayoutResource;
    }

    protected void setOpponentAttachmentMessageLayoutResource(@LayoutRes int typeOpponentAttachmentMessageLayoutResource) {
        this.typeOpponentAttachmentMessageLayoutResource = typeOpponentAttachmentMessageLayoutResource;
    }

    protected void setMessageLayoutResourceByType(ViewTypes typeLayout, @LayoutRes int messageLayoutResource) {
        switch (typeLayout) {
            case TYPE_OWN_MESSAGE:
                typeOwnMessageLayoutResource = messageLayoutResource;
                break;
            case TYPE_OPPONENT_MESSAGE:
                typeOpponentMessageLayoutResource = messageLayoutResource;
                break;
            case TYPE_ATTACHMENT_MESSAGE_OWN:
                typeOwnAttachmentMessageLayoutResource = messageLayoutResource;
                break;
            case TYPE_ATTACHMENT_MESSAGE_OPPONENT:
                typeOpponentMessageLayoutResource = messageLayoutResource;
                break;
            default:
                break;
        }
    }

    public void setWidgetLayoutResource(@LayoutRes int widgetLayoutResId) {
        if (widgetLayoutResId != this.widgetLayoutResId) {
            this.widgetLayoutResId = widgetLayoutResId;
        }
    }

    @Override
    public void onBindViewHolder(QBMessageViewHolder holder, int position) {
        QBChatMessage chatMessage = getItem(position);
        ViewTypes valueType = ViewTypes.values()[getItemViewType(position)];
        switch (valueType) {
            case TYPE_ATTACHMENT_MESSAGE_OWN:
                if (useEmbeddedImageLoader) {
                    Log.i(TAG, "onBindViewHolder TYPE_ATTACHMENT_MESSAGE_OPPONENT");
                    onBindViewAttachOwnHolder((AttachOwnHolder) holder, position);
                }
                break;
            case TYPE_ATTACHMENT_MESSAGE_OPPONENT:
                if (useEmbeddedImageLoader) {
                    Log.i(TAG, "onBindViewHolder TYPE_ATTACHMENT_MESSAGE_OPPONENT");
                    onBindViewAttachOpponentHolder((AttachOpponentHolder) holder, position);
                }
                break;
            case TYPE_OWN_MESSAGE:
                onBindViewMsgOwnHolder((MessageOwnHolder) holder, position);
                break;
            case TYPE_OPPONENT_MESSAGE:
                onBindViewMsgOpponentHolder((MessageOpponentHolder) holder, position);
                break;
            case TYPE_ATTACHMENT_CUSTOM:
                Log.i(TAG, "onBindViewHolder TYPE_ATTACHMENT_CUSTOM");
            default:
                break;
        }

    }

    protected void onBindViewAttachOwnHolder(AttachOwnHolder holder, int position) {
        QBChatMessage chatMessage = getItem(position);
        initGlideRequestListener(holder, ViewTypes.TYPE_ATTACHMENT_MESSAGE_OWN);
        showAttachment(holder, chatMessage, ViewTypes.TYPE_ATTACHMENT_MESSAGE_OWN);
    }

    protected void onBindViewAttachOpponentHolder(AttachOpponentHolder holder, int position) {
        QBChatMessage chatMessage = getItem(position);
        initGlideRequestListener(holder, ViewTypes.TYPE_ATTACHMENT_MESSAGE_OPPONENT);
        showAttachment(holder, chatMessage, ViewTypes.TYPE_ATTACHMENT_MESSAGE_OPPONENT);
    }

    protected void onBindViewMsgOpponentHolder(MessageOpponentHolder holder, int position) {
        QBChatMessage chatMessage = getItem(position);

        holder.messageTextView.setText((textOpp == null) ? chatMessage.getBody() : textOpp);
        holder.timeTextMessageTextView.setText((timeOpp == null) ? getDate(chatMessage.getDateSent() * 1000) : timeOpp);
    }

    protected void onBindViewMsgOwnHolder(MessageOwnHolder holder, int position) {
        QBChatMessage chatMessage = getItem(position);
        if (textOwnSize != 0) {
            Log.d(TAG, "textOwnSize= "+textOwnSize);
            holder.messageTextView.setTextSize(textOwnSize);
        }
        if (textOwnColor != 0) {
            holder.messageTextView.setTextColor(textOwnColor);
        }

        holder.messageTextView.setText((textOwn == null) ? chatMessage.getBody() : textOwn);
        holder.timeTextMessageTextView.setText((timeOwn == null) ? getDate(chatMessage.getDateSent() * 1000) : timeOwn);
    }

    // например можно сетить свой текст
    protected void setTextOwnText(String str) {
        textOwn = str;
    }

    protected void setTimeOwnText(String str) {
        timeOwn = str;
    }

    protected void setTextOpponentText(String str) {
        textOpp = str;
    }

    protected void setTimeOpponentText(String str) {
        timeOpp = str;
    }


    // например можно предопределить самые юзаемые методы
    protected void setTextOwnSize(float size) {
        textOwnSize = size;
    }

    protected void setTextOwnColor(int color) {
        textOwnColor = color;
    }


    public void setUseGlideImageLoader(boolean flag) {
        useEmbeddedImageLoader = flag;
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public QBChatMessage getItem(int position) {
        return chatMessages.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        QBChatMessage chatMessage = getItem(position);

        if (hasAttachments(chatMessage)) {
            QBAttachment attachment = chatMessage.getAttachments().iterator().next();
            Log.d("QBMessagesAdapter", "attachment.getType= " + attachment.getType());

            if (attachment.getType() != null && attachment.getType().equals(QBAttachment.PHOTO_TYPE)) {
                if (isIncoming(chatMessage)) {
                    return ViewTypes.TYPE_ATTACHMENT_MESSAGE_OPPONENT.ordinal();
                } else {
                    return ViewTypes.TYPE_ATTACHMENT_MESSAGE_OWN.ordinal();
                }
            } else {
                return ViewTypes.TYPE_ATTACHMENT_CUSTOM.ordinal();
            }

        } else {
            if (isIncoming(chatMessage)) {
                return ViewTypes.TYPE_OPPONENT_MESSAGE.ordinal();
            } else {
                return ViewTypes.TYPE_OWN_MESSAGE.ordinal();
            }
        }
    }

    @Override
    public void add(QBChatMessage item) {
        chatMessages.add(item);
        notifyDataSetChanged();
    }

    @Override
    public List<QBChatMessage> getList() {
        return chatMessages;
    }

    @Override
    public void addList(List<QBChatMessage> items) {
        chatMessages.addAll(0, items);
        notifyDataSetChanged();
    }

    private boolean isIncoming(QBChatMessage chatMessage) {
        QBUser currentUser = QBChatService.getInstance().getUser();
        return chatMessage.getSenderId() != null && !chatMessage.getSenderId().equals(currentUser.getId());
    }

    private boolean hasAttachments(QBChatMessage chatMessage) {
        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        return attachments != null && !attachments.isEmpty();
    }


    public String getDate(long milliseconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd", Locale.getDefault());
        return dateFormat.format(new Date(milliseconds));
    }

    private void showAttachment(final QBMessageViewHolder holder, QBChatMessage chatMessage, ViewTypes type) {
        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        QBAttachment attachment = attachments.iterator().next();
        Glide.with(context)
                .load(attachment.getUrl())
                .listener(glideRequestListener)
                .override(preferredImageSizePreview, preferredImageSizePreview)
                .dontTransform()
                .error(R.drawable.ic_error)
                .into((type == ViewTypes.TYPE_ATTACHMENT_MESSAGE_OWN) ? ((AttachOwnHolder) holder).attach_imageView : ((AttachOpponentHolder) holder).attach_imageView);
    }

    private void initGlideRequestListener(final QBMessageViewHolder holder, final ViewTypes type) {
        glideRequestListener = new RequestListener() {
            QBMessageViewHolder viewHolder = (type == ViewTypes.TYPE_ATTACHMENT_MESSAGE_OWN) ? (AttachOwnHolder) holder : (AttachOpponentHolder) holder;

            @Override
            public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
                e.printStackTrace();
                viewHolder.attach_imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                viewHolder.attachmentProgressBar.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
                viewHolder.attach_imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                viewHolder.attachmentProgressBar.setVisibility(View.GONE);
                return false;
            }
        };
    }


    protected static class MessageOwnHolder extends QBMessageViewHolder {
        public View customLayout;

        public MessageOwnHolder(View itemView, LayoutInflater inflater, @LayoutRes int widgetLayoutResId, @IdRes int msgId, @IdRes int timeId) {
            super(itemView);
            setCustomWidget(itemView, inflater, widgetLayoutResId);
            messageTextView = (TextView) itemView.findViewById(msgId);
            timeTextMessageTextView = (TextView) itemView.findViewById(timeId);
        }

        private void setCustomWidget(View itemView, LayoutInflater inflater, @LayoutRes int widgetLayoutResId) {
            final ViewGroup widgetFrame = (ViewGroup) itemView.findViewById(R.id.widget_frame);

            if (widgetFrame != null) {
                if (widgetLayoutResId != 0) {
                    customLayout = inflater.inflate(widgetLayoutResId, widgetFrame);
                } else {
                    widgetFrame.setVisibility(View.GONE);
                }
            }
        }
    }

    protected static class MessageOpponentHolder extends QBMessageViewHolder {

        public MessageOpponentHolder(View itemView, @IdRes int msgId, @IdRes int timeId) {
            super(itemView);
            messageTextView = (TextView) itemView.findViewById(msgId);
            timeTextMessageTextView = (TextView) itemView.findViewById(timeId);
        }
    }

    protected static class AttachOwnHolder extends QBMessageViewHolder {

        public AttachOwnHolder(View itemView, @IdRes int attachId, @IdRes int progressBarId) {
            super(itemView);
            attach_imageView = (ImageView) itemView.findViewById(attachId);
            attachmentProgressBar = (ProgressBar) itemView.findViewById(progressBarId);
        }
    }

    protected static class AttachOpponentHolder extends QBMessageViewHolder {

        public AttachOpponentHolder(View itemView, @IdRes int attachId, @IdRes int progressBarId) {
            super(itemView);
            attach_imageView = (ImageView) itemView.findViewById(attachId);
            attachmentProgressBar = (ProgressBar) itemView.findViewById(progressBarId);
        }
    }

    protected abstract static class QBMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView timeTextMessageTextView;
        public ImageView attach_imageView;
        public ProgressBar attachmentProgressBar;

        public QBMessageViewHolder(View itemView) {
            super(itemView);
        }
    }
}

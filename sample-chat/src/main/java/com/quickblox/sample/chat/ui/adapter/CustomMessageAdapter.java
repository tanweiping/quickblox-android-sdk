package com.quickblox.sample.chat.ui.adapter;

import android.content.Context;
import android.graphics.Color;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.sample.core.adapter.QBMessagesAdapter;

import java.util.List;

public class CustomMessageAdapter extends QBMessagesAdapter {

    public CustomMessageAdapter(Context context, List<QBChatMessage> chatMessages) {
        super(context, chatMessages);
    }

    @Override
    public void onBindViewHolder(QBMessageViewHolder holder, int position) {

//      setOwnMessageLayoutResource(R.layout.item_text_message_own);
//      setMessageLayoutResourceByType(ViewTypes.TYPE_OWN_MESSAGE, R.layout.item_text_message_own);

//      setTextOwnText("Mine text");

        setTextOwnSize(16);
        setTextOwnColor(Color.BLUE);
        super.onBindViewHolder(holder, position);
    }

//    @Override
//    protected void onBindViewMsgOwnHolder(MessageOwnHolder holder, int position) {
//        holder.messageTextView.setText("Groovy");
//        holder.timeTextMessageTextView.setText("time");
//    }
}

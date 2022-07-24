package corp.amq.hkd.ui.fragments.messages.adapter;

import android.view.View;

import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.utils.ShapeImageView;

import corp.amq.hkd.R;
import corp.amq.hkd.data.model.MessageDialog;

public class CustomDialogViewHolder extends DialogsListAdapter.DialogViewHolder<MessageDialog> {

    ShapeImageView imageView;

    public CustomDialogViewHolder(View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.dialogLastMessageUserAvatar);
    }

    @Override
    public void onBind(MessageDialog dialog) {
        super.onBind(dialog);
        imageView.setVisibility(View.GONE);
    }
}

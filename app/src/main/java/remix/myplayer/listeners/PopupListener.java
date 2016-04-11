package remix.myplayer.listeners;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

import remix.myplayer.R;
import remix.myplayer.activities.PlayListActivity;
import remix.myplayer.infos.MP3Info;
import remix.myplayer.infos.PlayListItem;
import remix.myplayer.utils.Constants;
import remix.myplayer.utils.DBUtil;
import remix.myplayer.utils.XmlUtil;

/**
 * Created by taeja on 16-1-25.
 */
public class PopupListener implements PopupMenu.OnMenuItemClickListener {
    private Context mContext;
    private int mId;
    //0:专辑 1:歌手 2:文件夹 3:播放列表
    private int mType;
    private String mKey;
    public PopupListener(Context Context,int id,int type,String key) {
        this.mContext = Context;
        this.mId = id;
        this.mType = type;
        this.mKey = key;
    }
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        ArrayList<MP3Info> list = new ArrayList<>();
        ArrayList<Long> ids = new ArrayList<Long>();
        String name = null;
        //根据不同参数获得mp3信息列表
        //艺术家与专辑
        if(mType == Constants.ARTIST_HOLDER || mType == Constants.ALBUM_HOLDER) {
            list = DBUtil.getMP3InfoByArtistIdOrAlbumId(mId, mType);
            for(MP3Info info : list)
                ids.add(info.getId());
        }
        //文件夹
        else if(mType == Constants.FOLDER_HOLDER) {
//            list = DBUtil.getMP3ListByFolder(DBUtil.mFolderList.get(mId));
            list = DBUtil.getMP3ListByIds(DBUtil.getIdsByFolderName(mKey,mId));
            for(MP3Info info : list)
                ids.add(info.getId());
        }
        //播放列表
        else {
            Iterator it = PlayListActivity.getPlayList().keySet().iterator();
            for(int i = 0 ; i <= mId ; i++) {
                it.hasNext();
                name = it.next().toString();
            }
            for(PlayListItem tmp : PlayListActivity.getPlayList().get(name))
                ids.add((long)tmp.getId());
        }
        switch (item.getItemId()) {
            //播放
            case R.id.menu_play:
                DBUtil.setPlayingList((ArrayList) ids.clone());
                Intent intent = new Intent(Constants.CTL_ACTION);
                Bundle arg = new Bundle();
                arg.putInt("Control", Constants.PLAYSELECTEDSONG);
                arg.putInt("Position", 0);
                intent.putExtras(arg);
                mContext.sendBroadcast(intent);
                break;
            //添加到播放列表
            case R.id.menu_add:
                DBUtil.mPlayingList.addAll(ids);
                DBUtil.setPlayingList(DBUtil.mPlayingList);
                break;
            //删除
            case R.id.menu_delete:
                if(mType != Constants.PLAYLIST_HOLDER)
                    DBUtil.deleteSong(mKey,mType);
                else {
                    if(name != null && !name.equals("")) {
                        PlayListActivity.getPlayList().remove(name);
                        if(PlayListActivity.mInstance != null && PlayListActivity.mInstance.getAdapter() != null)
                            PlayListActivity.mInstance.UpdateAdapter();
                        XmlUtil.updatePlaylist();
                    }
                }
                break;
            default:
                Toast.makeText(mContext, "Click " + item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
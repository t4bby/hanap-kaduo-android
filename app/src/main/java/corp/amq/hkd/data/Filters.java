package corp.amq.hkd.data;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import corp.amq.hkd.R;

public class Filters {
    public int rank;
    public List<Integer> roles;
    public int gender;

    public String filteredRank(Context context) {
        String[] rankArray = context.getResources().getStringArray(R.array.rank_array_filter);
        if(this.rank == 0) {
            return null;
        }
        return rankArray[this.rank];
    }

    public ArrayList<String> filterRoles(Context context) {
        String[] roleArray = context.getResources().getStringArray(R.array.role_array);
        ArrayList<String> arrayList = new ArrayList<>();
        for (Integer i: roles) {
            arrayList.add(roleArray[i]);
        }
        return arrayList;
    }

    public String filterGender(Context context) {
        String[] genderArray = context.getResources().getStringArray(R.array.gender_array_filter);
        if(this.gender == 0) {
            return null;
        }
        return genderArray[this.gender];
    }

}

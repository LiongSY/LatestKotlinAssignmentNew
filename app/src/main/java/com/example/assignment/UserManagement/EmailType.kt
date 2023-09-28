package com.example.assignment.UserManagement

import android.os.Parcel
import android.os.Parcelable

data class EmailType(val id: Long, var schoolName: String, var typeName: String) : Parcelable {


    constructor() : this(0, "", "")
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(schoolName)
        parcel.writeString(typeName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EmailType> {
        override fun createFromParcel(parcel: Parcel): EmailType {
            return EmailType(parcel)
        }

        override fun newArray(size: Int): Array<EmailType?> {
            return arrayOfNulls(size)
        }
    }

}

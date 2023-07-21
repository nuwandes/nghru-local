package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class MessageCancel(
    @Expose @field:SerializedName("error") var error: String?,
    @Expose @field:SerializedName("message") var message: String?
) : Serializable, Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(error)
        parcel.writeString(message)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }

        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }
    }

}
data class Message(
    @Expose @field:SerializedName("error") var error: String?,
    @Expose @field:SerializedName("message") var message: String?,
    @Expose @field:SerializedName("data") var data: MessageData?
) : Serializable, Parcelable {

    constructor( error: String?, message: String?) : this(
        error = error,
        message = message,
        data = null
    ) {
    }
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(MessageData::class.java.classLoader)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(error)
        parcel.writeString(message)
        parcel.writeParcelable(data, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }

        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }
    }

}

data class MessageData(
    @Expose @field:SerializedName("message") var message: String?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MessageData> {
        override fun createFromParcel(parcel: Parcel): MessageData {
            return MessageData(parcel)
        }

        override fun newArray(size: Int): Array<MessageData?> {
            return arrayOfNulls(size)
        }
    }

}

data class MessageCheckSum(
    var error: Boolean,
    var message: String
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (error) 1 else 0)
        parcel.writeString(message)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MessageCheckSum> {
        override fun createFromParcel(parcel: Parcel): MessageCheckSum {
            return MessageCheckSum(parcel)
        }

        override fun newArray(size: Int): Array<MessageCheckSum?> {
            return arrayOfNulls(size)
        }
    }

}

package com.theost.workchat.data.models.state

enum class UserStatus(apiName: String) {
    IDLE("idle"), ONLINE("active"), OFFLINE("")
}
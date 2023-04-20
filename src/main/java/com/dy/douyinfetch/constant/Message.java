package com.dy.douyinfetch.constant;

/**
 * 消息枚举
 */
public enum Message {
    //消息
    WebcastChatMessage ("WebcastChatMessage"),
    //成员进入
    WebcastMemberMessage ("WebcastMemberMessage"),
    //礼物
    WebcastGiftMessage ("WebcastGiftMessage"),
    //房间排名
    WebcastRoomRankMessage("WebcastRoomRankMessage"),
    //房间状态
    WebcastRoomStatsMessage("WebcastRoomStatsMessage"),
    //连麦通知
    WebcastLinkMicMethod("WebcastLinkMicMethod"),
    //连麦pk结束
    WebcastLinkMicBattleFinishMethod("WebcastLinkMicBattleFinishMethod"),
    WebcastControlMessage("WebcastRoomStatsMessage"),
    WebcastRoomMessage("WebcastRoomMessage"),
    WebcastPullStreamUpdateMessage("WebcastPullStreamUpdateMessage"),
    WebcastRoomNotifyMessage("WebcastRoomNotifyMessage"),
    WebcastDecorationModifyMethod("WebcastDecorationModifyMethod"),
    WebcastLiveShoppingMessage("WebcastLiveShoppingMessage"),
    WebcastLiveEcomGeneralMessage("WebcastLiveEcomGeneralMessage"),
    WebcastDouplusIndicatorMessage("WebcastDouplusIndicatorMessage"),
    WebcastProductChangeMessage("WebcastProductChangeMessage"),
    WebcastDecorationUpdateMessage("WebcastDecorationUpdateMessage"),
    WebcastHotRoomMessage("WebcastHotRoomMessage"),
    //连麦通知
    LinkMicBattleMethod("LinkMicBattleMethod"),
    WebcastScreenChatMessage("WebcastScreenChatMessage"),
    WebcastAudioChatMessage("WebcastAudioChatMessage"),
    WebcastExhibitionChatMessage("WebcastExhibitionChatMessage"),
    LinkMicMethod("LinkMicMethod"),
    WebcastBattleTeamTaskMessage("WebcastBattleTeamTaskMessage"),
    WebcastLinkMicArmiesMethod("WebcastLinkMicArmiesMethod"),
    WebcastProfitInteractionScoreMessage("WebcastProfitInteractionScoreMessage"),
    WebcastUpdateFanTicketMessage("WebcastUpdateFanTicketMessage"),
    WebcastBindingGiftMessage("WebcastBindingGiftMessage"),
    WebcastRanklistHourEntranceMessage("WebcastRanklistHourEntranceMessage"),
    WebcastCommonTextMessage("WebcastCommonTextMessage"),
    WebcastRoomUserSeqMessage("WebcastRoomUserSeqMessage"),
    WebcastEmojiChatMessage("WebcastEmojiChatMessage"),
    WebcastFansclubMessage("WebcastFansclubMessage"),
    WebcastInRoomBannerMessage("WebcastInRoomBannerMessage"),
    WebcastRoomDataSyncMessage("WebcastRoomDataSyncMessage"),
    WebcastLuckyBoxMessage("WebcastLuckyBoxMessage"),
    WebcastLuckyBoxTempStatusMessage("WebcastLuckyBoxTempStatusMessage"),
    WebcastHotChatMessage("WebcastHotChatMessage"),
    WebcastNotifyEffectMessage("WebcastNotifyEffectMessage"),
    WebcastGiftVoteMessage("WebcastGiftVoteMessage"),
    WebcastLinkMessage("WebcastLinkMessage"),
    WebcastLotteryDrawResultEventMessage("WebcastLotteryDrawResultEventMessage"),
    WebcastQuizAudienceStatusMessage("WebcastQuizAudienceStatusMessage"),
    WebcastLinkerContributeMessage("WebcastLinkerContributeMessage"),
    //关注
    WebcastSocialMessage("WebcastSocialMessage"),
    //点赞
    WebcastLikeMessage("WebcastLikeMessage");

    private final String message;

    Message(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

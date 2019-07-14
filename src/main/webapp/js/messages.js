'use strict';

const MESSAGING = {
    listeners: [],
    messageQueue: [],
    messageProcessingIsInProgress: false
}

function addMessageListener({name, callback}) {
    MESSAGING.listeners.push({name:name, callback:callback})
}

function removeMessageListener(name) {
    MESSAGING.listeners = _.reject(MESSAGING.listeners, listener=>listener.name===name)
}

function sendMessage(targetPredicate, messageContent) {
    MESSAGING.messageQueue.push({targetPredicate:targetPredicate, messageContent:messageContent})
    if (!MESSAGING.messageProcessingIsInProgress) {
        MESSAGING.messageProcessingIsInProgress = true

        while (_.size(MESSAGING.messageQueue) > 0) {
            const currentMsg = _.first(MESSAGING.messageQueue)
            MESSAGING.messageQueue = _.rest(MESSAGING.messageQueue)
            _.each(MESSAGING.listeners, listener=>{
                if (currentMsg.targetPredicate(listener.name)) {
                    listener.callback(currentMsg.messageContent)
                }
            })
        }

        MESSAGING.messageProcessingIsInProgress = false
    }
}

const toListener = name => listenerName => listenerName === name
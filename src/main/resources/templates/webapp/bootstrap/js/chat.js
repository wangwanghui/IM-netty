let socket;
const groupChatName = "OCTV-群聊室";
let defaultHeadImg = "../img/dog.png";
let currentChatUserNick = groupChatName;
let currentChatUserId;

const myNick = GetQueryString("nick");
const webSocketUrl = "ws://114.132.77.125:9011/websocket?userId=" + myNick;
let me;

const GROUP_CHAT_MESSAGE_CODE = 2;
const SYSTEM_MESSAGE_CODE = 5;
const PRIVATE_CHAT_MESSAGE_CODE = 1;
const PING_MESSAGE_CODE = 13;

const TYPE_NORMAL_SYSTEM_MESSGAE = 1;
const TYPE_UPDATE_USERCOUNT_SYSTEM_MESSGAE = 2;
const TYPE_UPDATE_USERLIST_SYSTEM_MESSGAE = "30";
const TYPE_PERSONAL_SYSTEM_MESSGAE = 4;
var lockReconnect = false;//避免重复连接
var tt;

function systemMessage(data) {
    switch (data.type) {
        case TYPE_UPDATE_USERLIST_SYSTEM_MESSGAE:
            let users = data.ext.userList;
            $('#userCount').text("在线人数：" + users.length);
            let userList = $("#userList");
            let repeatBox = $("#repeatBox");
            let appendString;
            userList.text("");
            userList.append(
                '<div class="chat_item" onClick="chooseUser(null, null)" style="z-index: ">' +
                '<img class="avatar img-circle" src="../img/chatroom.png" style="height: 50px;width: 50px">' +
                '<img id="redPoint" class="img-circle" src="../img/redPoint.png" style="height: 10px;width: 10px;position: absolute;left: 60;display: none">' +
                '<div style="color: white;font-size: large">群聊室</div>' +
                '</div>');
            users.forEach(function (user) {
                userList.append(
                    '<div class="chat_item" onClick="chooseUser(\'' + user + '\',\'' + user + '\')">' +
                    '<img class="avatar img-circle" src=' + defaultHeadImg + ' style="height: 50px;width: 50px">' +
                    '<img id="redPoint-' + user + '" class="img-circle" src="../img/redPoint.png" style="height: 10px;width: 10px;position: absolute;left: 60;display: none">' +
                    '<div style="color: white;font-size: large">' + user + '</div>' +
                    '</div>');
                appendString =
                    ['<div class="box" id="box-' + user + '" style="display: none">',
                        '    <div class="textareaHead" id="textareaHead">' + user + '</div>',
                        '    <div class="textarea scroll" id="responseContent-' + user + '"></div>',
                        '    <form onSubmit="return false;">',
                        '        <label>',
                        '            <textarea class="box_ft" name="message" id="sendTextarea-' + user + '"></textarea>',
                        '        </label>',
                        '        <div class="send"><button class="sendButton" onClick="sendMessageToUser(this.form.message.value, currentChatUserId)">发送</button>',
                        '        <button  onClick="sendMessageEmojiToUser(this.form.message.value,currentChatUserId)">发送表情</button></div>',
                        '    </form>',
                        '</div>'].join("");
                repeatBox.append(appendString);
            });
            break;
    }
}

function websocket() {
	me = new Object();
	me.userId = GetQueryString('nick')
    if (!window.WebSocket) {
        window.WebSocket = window.MozWebSocket;
    }
    if (window.WebSocket) {
        socket = new WebSocket(webSocketUrl);
        socket.onmessage = function (event) {
              console.log("event.data  --->"+event.data);
            let data = JSON.parse(event.data);
            console.log(JSON.stringify(data));
            console.log("ZZZZZZZZ" + Array.isArray(data))
               if(Array.isArray(data)){
                               for(var i in data) {
                             if (data.sendUserId !== me.userId) {
                                $("#responseContent-" + data[i].sendUserId).append(
                                        "   <div class='chatMessageBox'>" +
                                        "       <img class='chatAvatar' src='" + defaultHeadImg + "'>" +
                                        "       <div class='chatTime'>" + data[i].sendUserId + "&nbsp;&nbsp;  " + data[i].sendTime + "</div>" +
                                        "       <div class='chatMessgae'><span>" + uncodeUtf16(data[i].msg) + "</span></div>" +
                                        "   </div>");
                                } else {
                                console.log("AAA" +me.userId)
                                    $("#responseContent-" + data[i].receiverUserId).append(
                                        "   <div class='chatMessageBox_me'>" +
                                        "       <img class='chatAvatar_me' src=" + defaultHeadImg + ">" +
                                        "       <div class='chatTime'>" + data[i].sendTime + "&nbsp;&nbsp; " + data[i].sendUserId + "</div>" +
                                        "       <div class='chatMessgae_me'><span>" + uncodeUtf16(data[i].msg) + "</span></div>" +
                                        "   </div>");
                                }
                               }
                            }
            switch (data.code) {
                case GROUP_CHAT_MESSAGE_CODE:
                    if (data.sendUserId !== me.userId) {
                        $("#responseContent").append(
                            "   <div class='chatMessageBox'>" +
                            "       <img class='chatAvatar' src='" + defaultHeadImg + "'>" +
                            "       <div class='chatTime'>" + data.sendUserId + "&nbsp;&nbsp; " + data.sendTime + "</div>" +
                            "       <div class='chatMessgae'><span>" + uncodeUtf16(data.msg) + "</span></div>" +
                            "   </div>");
                    } else {
                        $("#responseContent").append(
                            "   <div class='chatMessageBox_me'>" +
                            "       <img class='chatAvatar_me' src='" + defaultHeadImg + "'>" +
                            "       <div class='chatTime'>" + data.sendTime + "&nbsp;&nbsp; " + "我" + "</div>" +
                            "       <div class='chatMessgae_me'><span>" + uncodeUtf16(data.msg) + "</span></div>" +
                            "   </div>");
                    }
                    updateRedPoint(null);
                    boxScroll(document.getElementById("responseContent"));
                    break;
                case SYSTEM_MESSAGE_CODE:
                    systemMessage(data);
                    boxScroll(document.getElementById("responseContent"));
                    break;
                case PING_MESSAGE_CODE:
                	sendPong();
                	break;
                case PRIVATE_CHAT_MESSAGE_CODE:
                    if (data.sendUserId !== me.userId) {
                    console.log("bbbb" + me.userId)
                    $("#responseContent-" + data.sendUserId).append(
                            "   <div class='chatMessageBox'>" +
                            "       <img class='chatAvatar' src='" + defaultHeadImg + "'>" +
                            "       <div class='chatTime'>" + data.sendUserId + "&nbsp;&nbsp;  " + data.sendTime + "</div>" +
                            "       <div class='chatMessgae'><span>" + uncodeUtf16(data.msg) + "</span></div>" +
                            "   </div>");
                    } else {
                    console.log("AAA" +me.userId)
                        $("#responseContent-" + data.receiverUserId).append(
                            "   <div class='chatMessageBox_me'>" +
                            "       <img class='chatAvatar_me' src=" + defaultHeadImg + ">" +
                            "       <div class='chatTime'>" + data.sendTime + "&nbsp;&nbsp; " + "我" + "</div>" +
                            "       <div class='chatMessgae_me'><span>" + uncodeUtf16(data.msg) + "</span></div>" +
                            "   </div>");
                    }

                    if (data.sendUserId != me.userId) {
                     console.log("ccc");
                    	updateRedPoint(data.sendUserId);
                    }
                    //boxScroll(document.getElementById("responseContent-" + data.sendUserId));
                    break;
                
            }
        };
        socket.onopen = function () {
           // 成功创建链接后，重置心跳检测
            heartCheck.reset().start();
            console.log('connected successfully')
            loginSend();
        };
        socket.onclose = function () {
            reconnect();
            quitSend();
        };
        return true;
    } else {
        alert("您的浏览器不支持WebSocket");
        return false;
    }
}

function loginSend() {
    let object = {};
    object.code = 3;
    object.sendUserId = myNick;
    send(JSON.stringify(object));
}

function quitSend() {
    let object = {};
    object.code = 3;
    object.sendUserId = myNick;
    send(JSON.stringify(object));
}


function sendPong() {
    let object = {};
    object.code = 40;
    object.type="heat-beat"
    send(JSON.stringify(object));
}

function sendMessageToUser(message, id) {
    if (message === "" || message == null) {
        alert("信息不能为空~");
        return;
    }
    let object = {};
    object.code = 1;
    object.sendUserId = me.userId;
    object.receiverUserId = id;
    object.msg = message;
    $('#sendTextarea-' + id).val("");
    send(JSON.stringify(object));
}


function sendMessageEmojiToUser(message,id) {
//    if (message === "" || message == null) {
//        alert("信息不能为空~");
//        return;
//    }
    let str = randomEmoji();
    let msg = message!=null ? message + str : str;
    let object = {};
    object.code = 1;
    object.sendUserId = me.userId;
    object.receiverUserId = id;
    object.msg = msg;
    $('#sendTextarea-' + id).val("");
    send(JSON.stringify(object));
}

function randomEmoji(){
let str = ['&#128517;','&#129315;','&#128516;','&#128514;']
let i = Math.round(Math.random()*3);
console.log("rrrrrrrrr"  + str[i]);
return str[i];
}

function uncodeUtf16(str){
    var reg = /\&#.*?;/g;
    var result = str.replace(reg,function(char){
        var H,L,code;
        if(char.length == 9 ){
            code = parseInt(char.match(/[0-9]+/g));
            H = Math.floor((code-0x10000) / 0x400)+0xD800;
            L = (code - 0x10000) % 0x400 + 0xDC00;
            return unescape("%u"+H.toString(16)+"%u"+L.toString(16));
        }else{
            return char;
        }
    });
        console.log(result);
    return result;
}

 function utf16toEntities(str) {
    var patt=/[\ud800-\udbff][\udc00-\udfff]/g; // 检测utf16字符正则
    str = str.replace(patt, function(char){
        var H, L, code;
        if (char.length===2) {
            H = char.charCodeAt(0); // 取出高位
            L = char.charCodeAt(1); // 取出低位
            code = (H - 0xD800) * 0x400 + 0x10000 + L - 0xDC00; // 转换算法
            return "&#" + code + ";";
        } else {
            return char;
        }
    });
    console.log(str);
    return str;
}


function sendMessage(message) {
    if (message === "" || message == null) {
        alert("信息不能为空~");
        return;
    }
    let object = {};
    object.code = 2;
    object.groupID = 10 ;
    object.sendUserId = myNick;
    object.msg = message;
    object.sendUserId = me.userId;
    $('#sendTextarea').val("");
    send(JSON.stringify(object));
}

function sendEmoji(message){
    let object = {};
    object.code = 2;
    object.groupID = 10 ;
    object.sendUserId = myNick;
    let emojiStr = randomEmoji();
    let msg = message!=null ? message + emojiStr : emojiStr;
    object.msg = msg;
    object.sendUserId = me.userId;
    $('#sendTextarea').val("");
    send(JSON.stringify(object));
}

function send(message) {
    if (!window.WebSocket) {
        return;
    }
    if (socket.readyState === WebSocket.OPEN) {
        socket.send(message);
    } else {
        alert("WebSocket连接没有建立成功！！");
    }
}

function chooseUser(username, id) {
    let box = $("#box");
    if (username != null) {
        $("#redPoint-" + id).css("display", "none");
        if (currentChatUserNick === groupChatName) {
            $("#box-" + id).css("display", "block");
            box.css("display", "none");
            currentChatUserNick = username;
            currentChatUserId = id;
        } else if (currentChatUserNick !== groupChatName && currentChatUserId !== id) {
            $("#box-" + id).css("display", "block");
            $("#box-" + currentChatUserId).css("display", "none");
            currentChatUserNick = username;
            currentChatUserId = id;
        }
    } else if (username === null && currentChatUserNick !== groupChatName) {
        $("#redPoint").css("display", "none");
        $("#box-" + id).css("display", "none");
        box.css("display", "block");
        currentChatUserNick = groupChatName;
    }
}

/**
 * 新消息红点提醒
 * @param id
 */
function updateRedPoint(id) {
    if (id == null && currentChatUserNick !== groupChatName) {
        $("#redPoint").css("display", "block");
    } else if (currentChatUserId !== id && id !== me.id) {
        $("#redPoint-" + id).css("display", "block");
    }
}

/**
 * Get 请求获取参数
 * @return {null}
 * @param name 参数名
 */
function GetQueryString(name) {
    var url = window.location.search;
    //var value = decodeURIComponent(window.location.search.split("?")[1]);
    var value  = decodeURIComponent(url);
    const reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    const r = value.substr(1).match(reg);
    if (r !== null) {
      console.log("find name: " +r[2])
      return r[2];
    }
    return null;
}



// 重新连接
        function reconnect() {
            if(lockReconnect) {
                return;
            };
            lockReconnect = true;
            //没连接上会一直重连，设置延迟避免请求过多
            // 清空延时器
            tt && clearTimeout(tt);
            tt = setTimeout(function () {
            	// 5秒后又继续创建连接
                websocket();
                lockReconnect = false;
            }, 5000);
        }

 // 心跳检测, 每隔一段时间检测链接状态，若是处于链接中，就向server端主动发送消息，来重置server端与客户端的最大链接时间，若是已经断开了，发起重连。
  var heartCheck = {
    // 心跳，比server端设置的链接时间稍微小一点，在接近断开的状况下以通讯的方式去重置链接时间。
    timeout: 60000,
    serverTimeoutObj: null,
    reset: function() {
      clearTimeout(this.serverTimeoutObj)
      return this
    },
    start: function() {
      this.serverTimeoutObj = window.setInterval(() => {
        if (socket.readyState === 1) {
        var ping = {"type":0}
         socket.send(JSON.stringify(ping));
        } else {
          console.log('websocket stop', socket.readyState)
          window.clearTimeout(this.serverTimeoutObj)
          websocket();
        }
      }, this.timeout)
    }
  }

/**
 * 滚动条置底
 * @param o document.getElementById("id")
 */
function boxScroll(o) {
    o.scrollTop = o.scrollHeight;
}
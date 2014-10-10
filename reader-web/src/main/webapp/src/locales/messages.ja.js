{
  "app": "Sismics Reader",
  "error": {
    "unknown": "アクセスエラー　再試行してください",
    "feed": "ログインエラー　再試行してください"
  },
  "login": {
    "username": "ユーザー名",
    "password": "パスワード",
    "remember": "ログイン状態を維持する",
    "submit": "ログイン",
    "error": "ユーザー名またはパスワードが違います"
  },
  "defaultpassword": {
    "info": "初めにユーザー名とパスワード欄に\"admin\"を入力してください。次に再度ユーザー名とパスワードの再登録をしてください。\"admin\"のままでは安全ではありません。",
    "warning": "まだパスワードの変更が済んでいません。セキュリティ維持の為、パスワードを変更してください。",
    "dismiss": "また後で"
  },
  "subscription": {
    "latest": "最新記事",
    "unread": "未読",
    "all": "すべて",
    "starred": "お気に入り",
    "subscriptions": "登録フィード",
    "empty": "登録フィードがありません",
    "emptyunread": "未読フィードはありません",
    "showall": "全てを表示する",
    "toggleunread": "既読フィードを　表示/非表示",
    "add": {
      "button": "フィードを追加",
      "placeholder": "フィードまたはサイトのURLを貼り付け",
      "submit": "追加"
    },
    "edit": {
      "button": "編集",
      "placeholder": "タイトル",
      "submit": "OK",
      "delete": "削除",
      "deleteconfirm": "この登録フィードを本当に削除しますか?"
    }
  },
  "category": {
    "nonesting": "カテゴリーを追加することはできません",
    "empty": "カテゴリーがありません",
    "add": {
      "placeholder": "名前",
      "submit": "追加",
      "button": "カテゴリーを追加"
    },
    "edit": {
      "button": "編集",
      "placeholder": "名前",
      "submit": "OK",
      "delete": "削除",
      "deleteconfirm": "この登録フィードを本当に削除しますか?"
    }
  },
  "toolbar": {
    "showall": "すべて",
    "shownew": "未読",
    "allread": "全て既読にする",
    "category": "カテゴリー",
    "back": "&larr; 戻る",
    "settings": "設定",
    "about": "アバウト",
    "logout": "ログアウト",
    "reportbug": "バグを報告する"
  },
  "settings": {
    "tabs": {
      "account": "アカウント",
      "importexport": "インポート・エクスポート",
      "users": "ユーザー"
    },
    "account": {
      "edit": {
        "title": "設定",
        "username": "ユーザー名",
        "locale": "言語",
        "theme": "テーマ",
        "password": "パスワード",
        "passwordconfirm": "パスワード（確認）",
        "submit": "登録",
        "cancel": "キャンセル",
        "success": "設定更新"
      }
    },
    "import": {
      "title": "インポート",
      "submit": "インポートする",
      "explain": "<a href=\"https://www.google.com/takeout/\" target=\"_blank\">Google Takeout</a>のZIPまたはOPMLのファイルを登録フィードにインポートするために送ってください",
      "success": "インポートが完了しました。まもなくフィードに反映されます。",
      "error": "インポートエラー"
    },
    "export": {
      "title": "エクスポート",
      "submit": "エクスポートする",
      "explain": "あなたのカテゴリーと登録フィードをエクスポートする"
    },
    "users": {
      "title": "ユーザー管理",
      "select": "ユーザを選んでください",
      "edit": {
        "newtitle": "新しいユーザー",
        "submit": "送信",
        "delete": "ユーザーを削除する",
        "deletebutton": "削除",
        "deleteconfirm": "このユーザーを本当に削除しますか?",
        "successNew": "新しいユーザーが追加されました",
        "successUpdate": "ユーザー更新"
      }
    }
  },
  "about": {
    "newupdate": "更新が可能です",
    "rebuildindex": {
      "title": "Rebuild search index",
      "message": "Rebuilding search index may take a while, but you can continue using the application.",
      "button": "Rebuild",
      "success": "Rebuilding articles index..."
    },
    "logs": {
      "title": "データログ",
      "level": {
        "all": "すべて",
        "fatal": "Fatal",
        "error": "Error",
        "warn": "Warn",
        "info": "Info",
        "debug": "Debug",
        "trace": "Trace"
      }
    },
    "license": {
      "title": "ライセンス",
      "content": "Reader is released under the terms of the GPL license. See <a href=\"http://opensource.org/licenses/GPL-2.0\" target=\"_blank\">http://opensource.org/licenses/GPL-2.0</a> for more information."
    },
    "informations": {
      "title": "詳細情報",
      "memory": "メモリー:",
      "version": "バージョン:",
      "website": "ウェブサイト :",
      "contact": "コンタクト:"
    }
  },
  "article": {
    "markasunread": "未読にする"
  },
  "feed": {
    "nomorearticles": "これ以上記事はありません",
    "nomoreunreadarticles": "これ以上未読記事はありません",
    "noarticle": "記事はありません",
    "nonewarticle": "全て既読！",
    "showall": "全てを表示",
    "retry": "再試行"
  },
  "wizard": {
    "title": "Sismics Reader インストールアシスタント",
    "subtitle": "Just a few setup steps before adding your first feed.",
    "step0title": "Change admin password",
    "step1title": "Setup network",
    "step2title": "Add first user",
    "step0explain": "You should change the default admin password, especially if you plan to use your Reader installation on an open network.",
    "step1explain": "Tick \"UPnP\" if you want Reader to try to open a port on your router.",
    "step2explain": "It's nearly over! Add your first user and start using Sismics Reader.",
    "upnp": "UPnP?",
    "upnperror": "Automatic port opening error, you need to open it manually",
    "previous": "前",
    "next": "次",
    "keepdefaultpassword": "Are you sure to keep the default password? It's a potential security risk!",
    "passwordconfirmerror": "パスワードが確認のものと一致しません",
    "passwordtooshort": "パスワードが短すぎます",
    "installationcompleted": "登録が完了しました !"
  },
  "theme": {
    "default": {
      "name": "Default"
    },
    "highcontrast": {
      "name": "High contrast"
    }
  }
}
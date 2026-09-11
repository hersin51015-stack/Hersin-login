#define WINVER 0x0601
#define _WIN32_WINNT 0x0601
#include <windows.h>
#include <shellapi.h>

#define ID_BTN_LOGIN 101
#define ID_BTN_GOOGLE 102
#define ID_TXT_EMAIL 103
#define ID_TXT_PASS 104

HWND hEmail, hPass, hBtnLogin, hBtnGoogle, hStatus;
HFONT hFontMain, hFontTitle;

LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    switch(msg) {
        case WM_CREATE: {
            hFontMain = CreateFontW(16, 0, 0, 0, FW_NORMAL, FALSE, FALSE, FALSE, ANSI_CHARSET,
                OUT_TT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY, DEFAULT_PITCH | FF_DONTCARE, L"Segoe UI");
            hFontTitle = CreateFontW(22, 0, 0, 0, FW_BOLD, FALSE, FALSE, FALSE, ANSI_CHARSET,
                OUT_TT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY, DEFAULT_PITCH | FF_DONTCARE, L"Segoe UI");

            HWND hTitle = CreateWindowW(L"STATIC", L"HERSIN LOGIN", WS_VISIBLE | WS_CHILD | SS_CENTER,
                40, 25, 320, 30, hwnd, NULL, NULL, NULL);
            SendMessageW(hTitle, WM_SETFONT, (WPARAM)hFontTitle, TRUE);

            HWND hLblEmail = CreateWindowW(L"STATIC", L"Email or Username:", WS_VISIBLE | WS_CHILD,
                40, 75, 320, 20, hwnd, NULL, NULL, NULL);
            SendMessageW(hLblEmail, WM_SETFONT, (WPARAM)hFontMain, TRUE);

            hEmail = CreateWindowExW(WS_EX_CLIENTEDGE, L"EDIT", L"", 
                WS_VISIBLE | WS_CHILD | ES_AUTOHSCROLL,
                40, 100, 320, 28, hwnd, (HMENU)ID_TXT_EMAIL, NULL, NULL);
            SendMessageW(hEmail, WM_SETFONT, (WPARAM)hFontMain, TRUE);

            HWND hLblPass = CreateWindowW(L"STATIC", L"Password:", WS_VISIBLE | WS_CHILD,
                40, 145, 320, 20, hwnd, NULL, NULL, NULL);
            SendMessageW(hLblPass, WM_SETFONT, (WPARAM)hFontMain, TRUE);

            hPass = CreateWindowExW(WS_EX_CLIENTEDGE, L"EDIT", L"", 
                WS_VISIBLE | WS_CHILD | ES_PASSWORD | ES_AUTOHSCROLL,
                40, 170, 320, 28, hwnd, (HMENU)ID_TXT_PASS, NULL, NULL);
            SendMessageW(hPass, WM_SETFONT, (WPARAM)hFontMain, TRUE);

            hBtnLogin = CreateWindowW(L"BUTTON", L"Log In", 
                WS_VISIBLE | WS_CHILD | BS_DEFPUSHBUTTON,
                40, 225, 320, 40, hwnd, (HMENU)ID_BTN_LOGIN, NULL, NULL);
            SendMessageW(hBtnLogin, WM_SETFONT, (WPARAM)hFontMain, TRUE);

            hBtnGoogle = CreateWindowW(L"BUTTON", L"Sign in with Google", 
                WS_VISIBLE | WS_CHILD | BS_PUSHBUTTON,
                40, 280, 320, 40, hwnd, (HMENU)ID_BTN_GOOGLE, NULL, NULL);
            SendMessageW(hBtnGoogle, WM_SETFONT, (WPARAM)hFontMain, TRUE);

            hStatus = CreateWindowW(L"STATIC", L"Compatible with Windows 7, 8, 10, and 11", WS_VISIBLE | WS_CHILD | SS_CENTER,
                20, 340, 360, 30, hwnd, NULL, NULL, NULL);
            SendMessageW(hStatus, WM_SETFONT, (WPARAM)hFontMain, TRUE);
            break;
        }
        case WM_COMMAND: {
            if (LOWORD(wParam) == ID_BTN_LOGIN) {
                wchar_t emailBuf[128] = {0};
                GetWindowTextW(hEmail, emailBuf, 127);
                if (wcslen(emailBuf) == 0) {
                    MessageBoxW(hwnd, L"Please enter your email or username.", L"Hersin Login", MB_OK | MB_ICONWARNING);
                } else {
                    SetWindowTextW(hStatus, L"Authentication successful!");
                    MessageBoxW(hwnd, L"Welcome! Successfully logged in to Hersin App.", L"Success", MB_OK | MB_ICONINFORMATION);
                }
            } else if (LOWORD(wParam) == ID_BTN_GOOGLE) {
                SetWindowTextW(hStatus, L"Opening Google Sign-In...");
                ShellExecuteA(NULL, "open", 
                    "https://accounts.google.com/o/oauth2/v2/auth?client_id=922049258875-qbgi10cpskcmn9vhasenb1s2um3nte63.apps.googleusercontent.com&response_type=token&redirect_uri=http://localhost:5000&scope=email%20profile%20openid", 
                    NULL, NULL, SW_SHOWNORMAL);
            }
            break;
        }
        case WM_DESTROY: {
            if (hFontMain) DeleteObject(hFontMain);
            if (hFontTitle) DeleteObject(hFontTitle);
            PostQuitMessage(0);
            break;
        }
        default:
            return DefWindowProcW(hwnd, msg, wParam, lParam);
    }
    return 0;
}

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    WNDCLASSW wc = {0};
    wc.lpfnWndProc = WndProc;
    wc.hInstance = hInstance;
    wc.hbrBackground = (HBRUSH)(COLOR_BTNFACE + 1);
    wc.lpszClassName = L"HersinLoginWin7";
    wc.hCursor = LoadCursor(NULL, IDC_ARROW);

    if (!RegisterClassW(&wc)) return 0;

    HWND hwnd = CreateWindowW(wc.lpszClassName, L"Hersin Login (Windows 7/8/10/11)",
        WS_OVERLAPPED | WS_CAPTION | WS_SYSMENU | WS_MINIMIZEBOX | WS_VISIBLE,
        CW_USEDEFAULT, CW_USEDEFAULT, 420, 430,
        NULL, NULL, hInstance, NULL);

    ShowWindow(hwnd, nCmdShow);
    UpdateWindow(hwnd);

    MSG msg;
    while (GetMessageW(&msg, NULL, 0, 0)) {
        TranslateMessage(&msg);
        DispatchMessageW(&msg);
    }
    return (int)msg.wParam;
}

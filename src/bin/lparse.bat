@echo off
setlocal DisableDelayedExpansion
set index=0
setlocal EnableDelayedExpansion

rem *** Take the cmd-line, remove all until the first parameter
rem *** Copy cmdcmdline without any modifications, as cmdcmdline has some strange behaviour
set "params=!cmdcmdline!"
set "params=!params:~0,-1!"
set "params=!params:*" =!"
echo params: !params!


pause

REM ** The exit is important, so the cmd.exe doesn't try to execute commands after ampersands
exit

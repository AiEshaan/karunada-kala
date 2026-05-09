
with open(r'c:\Users\Eshaan.P.M\karunada-kala\app\src\main\java\com\example\myapplication\ui\screens\MapScreen.kt', 'r') as f:
    content = f.read()
    open_braces = content.count('{')
    close_braces = content.count('}')
    print(f'Open: {open_braces}, Close: {close_braces}')

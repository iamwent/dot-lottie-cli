# kottie
A cli tool based on Koltin/Native to convert your Lottie JSON to dotLottie format.

```shell
./kottie -h
Usage: kottie [<options>] [<files>]...

  Convert Lottie JSON to dotLottie format

Options:
  -r, --recursive  convert directories recursively
  -h, --help       Show this message and exit
```

## Config cookie
LottieFiles modified the API for converting and requires authorization. So this a  
1. Open [Convert Lottie JSON](https://lottiefiles.com/tools/lottie-to-dotlottie) and login with your account
2. Open the developer console and get the cookie with `document.cookie`, single quotation marks are not needed
3. Save your cookie to the file `~/.kottie/cookie`
4. Enjoy

### Thanks to
- [Koltin/Native](https://kotlinlang.org/docs/native-overview.html)
- [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
- [Clikt](https://github.com/ajalt/clikt)
- [Okio](https://github.com/square/okio)
- [Ktor](https://ktor.io/)
- [to-dot-lottie](https://github.com/theapache64/to-dot-lottie)
- [.Lottie](https://dotlottie.io/)

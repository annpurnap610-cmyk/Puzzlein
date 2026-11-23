const BANNER_AD_HTML = `<!doctype html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <style>
      html, body {
        margin: 0;
        padding: 0;
        width: 320px;
        height: 50px;
        overflow: hidden;
        background: transparent;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    </style>
  </head>
  <body>
    <script>
      atOptions = {
        'key' : 'b17edb64d1936be8c547037f7f027631',
        'format' : 'iframe',
        'height' : 50,
        'width' : 320,
        'params' : {}
      };
    </script>
    <script data-cfasync="false" src="https://bicea.org/22/b17edb64d1936be8c547037f7f027631"></script>
  </body>
</html>`;

export function BannerAd() {
  return (
    <aside
      aria-label="Sponsored banner"
      className="border-t border-slate-800/80 bg-slate-950 py-3"
    >
      <div className="mx-auto flex max-w-5xl flex-col items-center justify-center gap-1 px-4">
        <div className="h-[50px] w-[320px] overflow-hidden rounded border border-slate-800/80 bg-slate-900/60">
          <iframe
            title="Live Banner Advertisement"
            srcDoc={BANNER_AD_HTML}
            width={320}
            height={50}
            scrolling="no"
            sandbox="allow-scripts allow-same-origin allow-popups allow-popups-to-escape-sandbox allow-forms allow-top-navigation-by-user-activation"
            className="block h-[50px] w-[320px] border-0"
          />
        </div>
      </div>
    </aside>
  );
}

import { ArrowRight, Check, Trophy } from 'lucide-react';
import { DIFFICULTY_OPTIONS, GridSize, RecordsBySize } from '../types/puzzle';
import { formatTime } from '../utils/puzzleLogic';

interface DifficultyScreenProps {
  selectedSize: GridSize;
  records: RecordsBySize;
  onSelectSize: (size: GridSize) => void;
  onStartGame: (size?: GridSize) => void;
}

function MiniGridPreview({ size, selected }: { size: GridSize; selected: boolean }) {
  const totalCells = size * size;
  const cells = Array.from({ length: totalCells }, (_, idx) => idx);

  return (
    <div
      aria-hidden="true"
      className={`grid gap-1 rounded-lg p-1.5 transition-colors duration-150 ${
        selected ? 'bg-slate-800/90' : 'bg-slate-950/80'
      }`}
      style={{
        gridTemplateColumns: `repeat(${size}, minmax(0, 1fr))`,
        width: '72px',
        height: '72px',
      }}
    >
      {cells.map((idx) => {
        const isBlank = idx === totalCells - 1;
        return (
          <div
            key={idx}
            className={`rounded-[3px] transition-colors duration-150 ${
              isBlank
                ? 'bg-slate-950/60 border border-slate-800/70'
                : selected
                  ? 'bg-amber-400/90'
                  : 'bg-slate-300/80 group-hover:bg-slate-200'
            }`}
          />
        );
      })}
    </div>
  );
}

export function DifficultyScreen({
  selectedSize,
  records,
  onSelectSize,
  onStartGame,
}: DifficultyScreenProps) {
  const activeOption =
    DIFFICULTY_OPTIONS.find((option) => option.size === selectedSize) ?? DIFFICULTY_OPTIONS[1];

  return (
    <section
      aria-labelledby="difficulty-heading"
      className="mx-auto flex w-full max-w-4xl flex-col gap-8"
    >
      <div className="flex flex-col gap-3 text-center sm:text-left">
        <p className="text-sm font-medium text-amber-400">Choose Your Board</p>
        <h1
          id="difficulty-heading"
          className="font-display text-3xl font-bold tracking-tight text-slate-50 sm:text-4xl [text-wrap:balance]"
        >
          Select a Grid Difficulty
        </h1>
        <p className="max-w-2xl text-base leading-relaxed text-slate-300">
          Pick a 3×3, 4×4, or 5×5 board to begin. Slide aligned tiles into the empty recess until
          every number sits in ascending order.
        </p>
      </div>

      <div
        role="radiogroup"
        aria-label="Puzzle grid size difficulty"
        className="grid grid-cols-1 gap-4 md:grid-cols-3"
      >
        {DIFFICULTY_OPTIONS.map((option) => {
          const isSelected = option.size === selectedSize;
          const record = records[option.size];

          return (
            <button
              key={option.size}
              type="button"
              role="radio"
              aria-checked={isSelected}
              onClick={() => onSelectSize(option.size)}
              onDoubleClick={() => onStartGame(option.size)}
              className={`group flex flex-col justify-between gap-5 rounded-xl border p-5 text-left transition-all duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-amber-400 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-950 cursor-pointer ${
                isSelected
                  ? 'border-amber-400 bg-slate-900 shadow-lg shadow-amber-950/20'
                  : 'border-slate-800 bg-slate-900/60 hover:border-slate-700 hover:bg-slate-900'
              }`}
            >
              <div className="flex items-start justify-between gap-4">
                <div className="flex flex-col gap-1">
                  <span
                    className={`text-xs font-semibold tracking-wide ${
                      isSelected ? 'text-amber-400' : 'text-slate-400'
                    }`}
                  >
                    {option.label}
                  </span>
                  <span className="font-display text-2xl font-bold text-slate-50 tabular-nums">
                    {option.title}
                  </span>
                  <span className="text-xs text-slate-400 tabular-nums">
                    {option.tileCount} numbered tiles
                  </span>
                </div>

                <MiniGridPreview size={option.size} selected={isSelected} />
              </div>

              <p className="text-sm leading-relaxed text-slate-300">{option.description}</p>

              <div className="flex items-center justify-between border-t border-slate-800/80 pt-3.5 text-xs">
                {record ? (
                  <span className="inline-flex items-center gap-1.5 font-mono text-slate-300 tabular-nums">
                    <Trophy className="h-3.5 w-3.5 text-amber-400 shrink-0" aria-hidden="true" />
                    <span>
                      {record.bestMoves} moves · {formatTime(record.bestTimeSeconds)}
                    </span>
                  </span>
                ) : (
                  <span className="text-slate-500">No record yet</span>
                )}

                <span
                  className={`inline-flex items-center gap-1 font-medium whitespace-nowrap shrink-0 ${
                    isSelected ? 'text-amber-400' : 'text-slate-500 group-hover:text-slate-300'
                  }`}
                >
                  {isSelected && <Check className="h-3.5 w-3.5" aria-hidden="true" />}
                  <span>{isSelected ? 'Selected' : 'Choose'}</span>
                </span>
              </div>
            </button>
          );
        })}
      </div>

      <div className="flex flex-col items-stretch justify-between gap-4 border-t border-slate-800/80 pt-6 sm:flex-row sm:items-center">
        <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-slate-400">
          <span>Click or tap aligned tiles</span>
          <span aria-hidden="true">·</span>
          <span>Arrow keys or WASD</span>
          <span aria-hidden="true">·</span>
          <span>Multi-tile row slide supported</span>
        </div>

        <button
          type="button"
          onClick={() => onStartGame(selectedSize)}
          className="inline-flex min-h-[44px] items-center justify-center gap-2 rounded-lg bg-amber-400 px-6 py-2.5 text-sm font-semibold text-slate-950 transition-colors duration-150 hover:bg-amber-300 active:bg-amber-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-amber-300 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-950 whitespace-nowrap shrink-0 cursor-pointer"
        >
          <span>Start {activeOption.size}×{activeOption.size} Puzzle</span>
          <ArrowRight className="h-4 w-4" aria-hidden="true" />
        </button>
      </div>
    </section>
  );
}
